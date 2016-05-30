package sbtdocker

import sbt._

object DockerKeys {
  val docker = taskKey[ImageId]("Build a Docker image.")
  val dockerBuildAndPush = taskKey[ImageId]("Build a Docker image and pushes it to a registry.")
  val dockerPush = taskKey[Unit]("Push a already built Docker image to a registry.")
  val dockerCreate = taskKey[ContainerId]("Create a docker container.")
  val dockerStart = taskKey[ContainerId]("Start a docker container")
  val dockerStop = taskKey[ContainerId]("Stop a docker container")
  val dockerRm = taskKey[ContainerId]("Remove a docker container")

  @deprecated("Use imageNames instead.", "1.0.0")
  val imageName = taskKey[ImageName]("Name of the built image.")

  val dockerfile = taskKey[DockerfileLike]("Definition of the Dockerfile that should be built.")
  val imageNames = taskKey[Seq[ImageName]]("Names of the built image.")
  val createOptions = taskKey[CreateOptions]("Options for the Docker create command.")
  val startOptions = taskKey[StartOptions]("Options for the docker start command.")
  val stopOptions = taskKey[StopOptions]("Options for the docker stop command.")
  val rmOptions = taskKey[RmOptions]("Options for the docker rm command")
  val dockerPath = settingKey[String]("Path to the Docker binary.")
  val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
}
