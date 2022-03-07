package sbtdocker.staging

import sbt._
import sbtdocker.DockerfileInstruction

object StagedDockerfile {
  def empty = StagedDockerfile(Seq.empty, Set.empty)
}

case class StagedDockerfile(instructions: Seq[DockerfileInstruction], stageFiles: Set[(SourceFile, File)]) {
  def addInstruction(instruction: DockerfileInstruction): StagedDockerfile = copy(instructions = instructions :+ instruction)

  def stageFile(source: SourceFile, destination: File): StagedDockerfile = copy(stageFiles = stageFiles + (source -> destination))

  def stageFiles(files: Set[(SourceFile, File)]): StagedDockerfile = copy(stageFiles = stageFiles ++ files)

  def instructionsString: String = instructions.mkString("\n")
}
