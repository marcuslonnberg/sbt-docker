package sbtdocker

import sbt._

object DockerFromFilePlugin extends AutoPlugin {

  object autoImport {
    val dockerFromFile = DockerKeys.dockerFromFile
    type DockerFromFile =  sbtdocker.DockerFromFile
  }

  override def projectSettings = DockerSettings.dockerFromFileSettings
}
