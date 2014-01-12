import sbtdocker.{Dockerfile, Plugin}
import Plugin._
import Plugin.DockerKeys._
import sbt._
import Keys._

name := "example-package-spray"

organization := "sbt-docker"

version := "0.1.0"

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= {
  val akkaV = "2.2.3"
  val sprayV = "1.2.0"
  Seq(
    "io.spray"           %  "spray-can"       % sprayV,
    "io.spray"           %  "spray-routing"   % sprayV,
    "com.typesafe.akka"  %% "akka-actor"      % akkaV,
    "com.typesafe.akka"  %% "akka-transactor" % akkaV
  )
}

dockerSettings

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package` in(Compile, packageBin))

// Tell docker at which path the jar file will be created
jarFile in docker <<= (artifactPath in(Compile, packageBin)).toTask

// Create a custom Dockerfile
dockerfile in docker <<= (stageDir in docker, jarFile in docker, managedClasspath in Compile, mainClass in Compile) map {
  (stageDir, jarFile, managedClasspath, mainClass) => new Dockerfile {
    implicit val impStageDir = stageDir
    from("totokaka/arch-java")
    // Expose port 8080
    expose(8080)
    // Copy all dependencies to 'libs' in stage dir
    val deps = managedClasspath.files.map {
      depFile =>
        val target = file("libs") / depFile.name
        copyToStageDir(depFile, target)
        target.getPath
    } mkString ":"
    // Add the libs dir
    add("libs", "libs")
    // Add the generated jar file
    add(jarFile, file(jarFile.name))
    // Set the entry point to start the application using the main class
    entryPoint("java", "-cp", s"$deps:${jarFile.name}", mainClass.get)
  }
}
