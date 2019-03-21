package sbtdocker.staging

import sbt._
import sbtdocker._
import sbtdocker.staging.DefaultDockerfileProcessor._

object DefaultDockerFromFileProcessor extends DockerSourceFileProcessor {

  def apply(dockerSourceFile: DockerFromFileInstructions, stageDir: File) = {
    dockerSourceFile.instructions.foldLeft(StagedDockerfile.empty)(handleInstruction(stageDir))
  }
}
