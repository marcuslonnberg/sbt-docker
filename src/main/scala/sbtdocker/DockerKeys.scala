package sbtdocker

import sbt._

object DockerKeys {
  val docker = taskKey[ImageId]("Build a Docker image.")
  val dockerBuildAndPush = taskKey[ImageId]("Build a Docker image and pushes it to a registry.")
  val dockerPush = taskKey[Unit]("Push a already built Docker image to a registry.")

  @deprecated("Use imageNames instead.", "1.0.0")
  val imageName = taskKey[ImageName]("Name of the built image.")

  val dockerfile = taskKey[DockerfileLike]("Definition of the Dockerfile that should be built.")
  val imageNames = taskKey[Seq[ImageName]]("Names of the built image.")
  val dockerPath = settingKey[String]("Path to the Docker binary.")
  val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
  val buildArgs = settingKey[Map[String, String]]("Build arguments (build-arg) for the Docker build command.")
}
