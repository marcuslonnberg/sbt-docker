package sbtdocker.staging

import java.io.File

import sbtdocker.DockerFromFileInstructions

trait DockerSourceFileProcessor {
  def apply(dockerFromFile: DockerFromFileInstructions, stageDir: File): StagedDockerfile
}
