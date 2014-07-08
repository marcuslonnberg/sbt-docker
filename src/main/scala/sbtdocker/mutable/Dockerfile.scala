package sbtdocker.mutable

import sbtdocker.{CopyPath, DockerfileLike, Instruction}

case class Dockerfile(var instructions: Seq[Instruction] = Seq.empty,
                      var stagedFiles: Seq[CopyPath] = Seq.empty) extends DockerfileLike[Dockerfile] {

  def addInstruction(instruction: Instruction) = {
    instructions :+= instruction
    this
  }

  def stageFile(copy: CopyPath) = {
    stagedFiles :+= copy
    this
  }
}
