package sbtdocker

import com.spotify.docker.client._
import sbt._

object DockerKeys {
  @deprecated("Use dockerImageNames instead", "2.0.0")
  val imageNames = taskKey[Seq[ImageName]]("Docker image names")
  val dockerImageNames = taskKey[Seq[ImageName]]("Docker image names")

  val dockerfile = taskKey[DockerfileLike]("Definition of a Dockerfile")

  @deprecated("Use dockerBuildOptions instead", "2.0.0")
  val buildOptions = settingKey[BuildOptions]("Options for the Docker build")
  val dockerBuildOptions = settingKey[BuildOptions]("Options for the Docker build")

  val dockerClient = taskKey[DockerClient]("Client to connect to a Docker host")

  val docker = taskKey[Unit]("Builds a Docker image")

  val dockerPush = taskKey[Unit]("Pushes a Docker image to a registry")

  val dockerBuildAndPush = taskKey[Unit]("Builds a Docker image and pushes it to a registry")
}
