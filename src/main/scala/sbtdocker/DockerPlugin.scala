package sbtdocker

import sbt._

object DockerPlugin extends AutoPlugin {
  object autoImport {
    val DockerKeys = sbtdocker.DockerKeys

    @deprecated("Use dockerImageNames instead", "2.0.0")
    val imageNames = DockerKeys.imageNames
    val dockerImageNames = DockerKeys.dockerImageNames
    val dockerfile = DockerKeys.dockerfile
    @deprecated("Use dockerBuildOptions instead", "2.0.0")
    val buildOptions = DockerKeys.buildOptions
    val dockerBuildOptions = DockerKeys.dockerBuildOptions
    val dockerClient = DockerKeys.dockerClient
    val docker = DockerKeys.docker
    val dockerPush = DockerKeys.dockerPush
    val dockerBuildAndPush = DockerKeys.dockerBuildAndPush

    type Dockerfile = sbtdocker.Dockerfile
    val ImageId = sbtdocker.ImageId
    type ImageId = sbtdocker.ImageId
    val ImageName = sbtdocker.ImageName
    type ImageName = sbtdocker.ImageName
    val BuildOptions = sbtdocker.BuildOptions
    type BuildOptions = sbtdocker.BuildOptions

    val CopyFile = sbtdocker.staging.CopyFile
    type CopyFile = sbtdocker.staging.CopyFile
  }

  override def projectSettings = DockerSettings.baseDockerSettings
}
