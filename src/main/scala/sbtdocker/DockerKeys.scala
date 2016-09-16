package sbtdocker

import sbt._

object DockerKeys {
  val docker = taskKey[ImageId]("Build a Docker image.")
  val dockerBuildAndCopy = taskKey[ImageId]("Builds a Docker image and copies it to a docker machine.")
  val dockerBuildAndPush = taskKey[ImageId]("Builds a Docker image and pushes it to a registry.")
  val dockerPush = taskKey[Unit]("Push an already built Docker image to a registry.")
  val dockerCopy = taskKey[Unit]("Copy an already built Docker image to a docker machine.")

  @deprecated("Use imageNames instead.", "1.0.0")
  val imageName = taskKey[ImageName]("Name of the built image.")

  val dockerfile = taskKey[DockerfileLike]("Definition of the Dockerfile that should be built.")
  val imageNames = taskKey[Seq[ImageName]]("Names of the built image.")
  val dockerPath = settingKey[String]("Path to the Docker binary.")
  val dockerMachinePath = settingKey[String]("Path to the Docker Machine binary.")
  val dockerMachineName = settingKey[String]("Name of docker machine to copy image to.")
  val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
}
