package sbtdocker.mutable

import sbtdocker.{DockerFromFileInstructions, Instruction}

case class DockerFromFile(var instructions: Seq[Instruction] = Seq.empty) extends DockerFromFileInstructions {
  type T = DockerFromFile

  def addInstruction(instruction: Instruction) = {
    instructions :+= instruction
    this
  }

  def addInstructions(instructions: TraversableOnce[Instruction]) = {
    this.instructions ++= instructions
    this
  }

  protected def self = this
}

