package sbtdocker

import sbt._

object DockerPlugin extends AutoPlugin {
  // Automatically enable the plugin
  override def trigger = allRequirements

  object autoImport {
    val DockerKeys = sbtdocker.DockerKeys

    val docker = DockerKeys.docker
    val dockerfile = DockerKeys.dockerfile
    val dockerCmd = DockerKeys.dockerCmd
    val imageNames = DockerKeys.imageNames
    val buildOptions = DockerKeys.buildOptions

    type Dockerfile = sbtdocker.Dockerfile
    val ImageId = sbtdocker.ImageId
    type ImageId = sbtdocker.ImageId
    val ImageName = sbtdocker.ImageName
    type ImageName = sbtdocker.ImageName
    val BuildOptions = sbtdocker.BuildOptions
    type BuildOptions = sbtdocker.BuildOptions

    def dockerAutoPackage(fromImage: String = "dockerfile/java",
                          exposePorts: Seq[Int] = Seq.empty): Seq[sbt.Def.Setting[_]] = {
      DockerSettings.packageDockerSettings(fromImage, exposePorts)
    }
  }

  override def projectSettings = DockerSettings.baseDockerSettings
}
