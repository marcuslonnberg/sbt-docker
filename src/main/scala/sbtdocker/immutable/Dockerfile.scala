package sbtdocker.immutable

import sbtdocker.{CopyPath, DockerfileLike, Instruction}

object Dockerfile {
  def empty = Dockerfile()
}

case class Dockerfile(instructions: Seq[Instruction] = Seq.empty,
                      stagedFiles: Seq[CopyPath] = Seq.empty) extends DockerfileLike[Dockerfile] {

  def addInstruction(instruction: Instruction) = Dockerfile(instructions :+ instruction, stagedFiles)

  def stageFile(copy: CopyPath) = Dockerfile(instructions, stagedFiles :+ copy)
}
