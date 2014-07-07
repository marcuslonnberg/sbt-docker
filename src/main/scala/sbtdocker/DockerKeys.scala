package sbtdocker

import sbt._

object DockerKeys {
  val docker = taskKey[ImageId]("Builds a Docker image.")

  val dockerfile = taskKey[Dockerfile]("Definition of the Dockerfile that should be built.")
  val imageName = taskKey[ImageName]("Name of the built image.")
  val dockerCmd = settingKey[String]("Path to the Docker binary.")
  val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
}
