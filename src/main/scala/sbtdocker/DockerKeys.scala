package sbtdocker

import sbt._

object DockerKeys {
  val docker = taskKey[ImageId]("Build a Docker image.")
  val dockerBuildAndPush = taskKey[ImageId]("Build a Docker image and pushes it to a registry.")
  val dockerPush = taskKey[Unit]("Push a already built Docker image to a registry.")

  val dockerfile = taskKey[DockerfileLike]("Definition of the Dockerfile that should be built.")
  val imageName = taskKey[ImageName]("Name of the built image.")
  val dockerCmd = settingKey[String]("Path to the Docker binary.")
  val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
}
