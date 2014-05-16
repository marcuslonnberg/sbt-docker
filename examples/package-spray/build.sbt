import sbtdocker.{Dockerfile, Plugin}
import Plugin.DockerKeys._
import sbt._

name := "example-package-spray"

organization := "sbtdocker"

version := "0.1.0"

resolvers ++= Seq("spray repo" at "http://repo.spray.io/")

libraryDependencies ++= {
  val akkaV = "2.2.3"
  val sprayV = "1.2.0"
  Seq(
    "io.spray" % "spray-can" % sprayV,
    "io.spray" % "spray-routing" % sprayV,
    "com.typesafe.akka" %% "akka-actor" % akkaV
  )
}

dockerSettings

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package` in(Compile, packageBin))

// Tell docker at which path the jar file will be created
jarFile in docker <<= (artifactPath in(Compile, packageBin)).toTask

// Define a Dockerfile
dockerfile in docker <<= (jarFile in docker, managedClasspath in Runtime, mainClass in Runtime) map {
  case (jarFile, managedClasspath, Some(mainClass)) => {
    val libs = "/app/libs"
    val jarTarget = "/app/" + jarFile.name
    new Dockerfile {
      // Use a base image that contains Java
      from("dockerfile/java")
      // Expose port 8080
      expose(8080)
      // Copy all dependencies to 'libs' in stage dir
      managedClasspath.files.foreach { depFile =>
        val target = file(libs) / depFile.name
        copyToStageDir(depFile, target)
      }
      // Add the libs dir
      add(libs, libs)
      // Add the generated jar file
      add(jarFile, jarTarget)
      // The classpath is the 'libs' dir and the produced jar file
      val classpath = s"$libs/*:$jarTarget"
      // Set the entry point to start the application using the main class
      cmd("java", "-cp", classpath, mainClass)
    }
  }
}
