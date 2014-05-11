import sbtdocker.{Dockerfile, Plugin}
import Plugin._
import Plugin.DockerKeys._
import sbt._
import Keys._

name := "example-package-simple"

organization := "sbtdocker"

version := "0.1.0"

dockerSettings

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package` in(Compile, packageBin))

// Tell docker at which path the jar file will be created
jarFile in docker <<= (artifactPath in(Compile, packageBin)).toTask

// Define a Dockerfile
dockerfile in docker <<= (stageDir in docker, jarFile in docker, managedClasspath in Runtime, mainClass in Compile) map {
  case (stageDir, jarFile, classpath, Some(mainClass)) => new Dockerfile {
    implicit val stageDirImplicit = stageDir
    from("dockerfile/java")
    val files = classpath.files.map { file =>
      val target = "/app/" + file.getName
      add(file, target)
      target
    }
    // Add the generated jar file
    private val jarTarget = file("/app") / jarFile.getName
    add(jarFile, jarTarget)
    // Run the jar file with scala library on the class path
    val classpathString = files.mkString(":") + ":" + jarTarget.getPath
    entryPoint("java", "-cp", classpathString, mainClass)
  }
}

// Set a custom image name
imageName in docker <<= (organization, name, version) map {(organization, name, version) =>
    s"$organization/$name:v$version"
}
