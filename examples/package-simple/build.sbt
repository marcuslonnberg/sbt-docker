import sbtdocker.{Dockerfile, Plugin}
import Plugin._
import Plugin.DockerKeys._
import sbt._
import Keys._

name := "example-package-simple"

organization := "sbt-docker"

version := "0.1.0"

dockerSettings

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package` in(Compile, packageBin))

// Tell docker at which path the jar file will be created
jarFile in docker <<= (artifactPath in(Compile, packageBin)).toTask

// Create a custom Dockerfile
dockerfile in docker <<= (stageDir in docker, jarFile in docker, mainClass in Compile) map {
  (stageDir, jarFile, mainClass) => new Dockerfile {
    from("totokaka/arch-java")
    // Install scala
    run("pacman", "-S", "--noconfirm", "scala")
    // Add the generated jar file
    add(jarFile, file(jarFile.getName))(stageDir)
    // Run the jar file with scala library on the class path
    entryPoint("java", "-cp", s"/usr/share/scala/lib/scala-library.jar:${jarFile.getName}", mainClass.get)
  }
}

// Set a custom image name
imageName in docker <<= (organization, name, version) map
  ((organization, name, version) => s"$organization/$name:v$version")