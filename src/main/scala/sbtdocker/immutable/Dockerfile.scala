package sbtdocker.immutable

import sbtdocker.Dockerfile.CopyPath
import sbtdocker.Instructions
import sbt._

import java.io.File

import sbtdocker.Instructions._

object Dockerfile {
  def empty = Dockerfile()
}

case class Dockerfile(instructions: Seq[Instructions.Instruction] = Seq.empty,
                      stagedFiles: Seq[CopyPath] = Seq.empty) extends DockerfileCommands {
  type T = Dockerfile

  def mkString = instructions.map(_.toInstructionString).mkString("\n")

  def addInstruction(instruction: Instruction) = {
    copy(instructions = instructions :+ instruction)
  }

  def stageFile(from: File, target: File) = {
    val cp = CopyPath(from, target)
    copy(stagedFiles = stagedFiles :+ cp)
  }

  def stageFile(from: File, target: String) = {
    val targetFile = expandPath(from, target)
    val cp = CopyPath(from, targetFile)
    copy(stagedFiles = stagedFiles :+ cp)
  }

  private def expandPath(from: File, target: String) = {
    val targetFile = file(target)
    if (target.endsWith("/")) targetFile / from.name
    else targetFile
  }
}

trait DockerfileCommands {
  type T <: DockerfileCommands

  def addInstruction(instruction: Instruction): T

  def stageFile(from: File, target: File): T

  def stageFile(from: File, target: String): T

  def add(from: File, to: File) = {
    addInstruction(Add(from.toString, to.toString))
      .stageFile(from, to)
  }

  def add(from: File, to: String) = {
    addInstruction(Add(from.toString, to.toString))
      .stageFile(from, to)
  }

  def run(args: String*) = addInstruction(Run(args: _*))

  def expose(ports: Int*) = addInstruction(Expose(ports: _*))

  def cmd(args: String*) = addInstruction(Cmd(args: _*))
}
