package sbtdocker.immutable

import sbtdocker.{DockerFromFileInstructions, Instruction}

object DockerFromFile {
  def empty = DockerFromFile()
}

case class DockerFromFile(instructions: Seq[Instruction] = Seq.empty) extends DockerFromFileInstructions {
  override type T = DockerFromFile

  override def addInstruction(instruction: Instruction) = DockerFromFile(instructions :+ instruction)

  protected def self = this
}
