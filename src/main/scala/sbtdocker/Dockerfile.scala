package sbtdocker

import sbt._
import java.nio.file.Path

object Dockerfile {

  case class CopyFile(source: File, targetRelative: File)

}

class Dockerfile extends DockerfileApi {

  import Dockerfile._
  import Instructions._

  var instructions = Seq.empty[Instruction]
  var pathsToCopy = Seq.empty[CopyFile]

  def addInstruction(instruction: Instruction) = instructions :+= instruction

  def copyToStageDir(source: File, targetRelativeToStageDir: File) = pathsToCopy :+= CopyFile(source, targetRelativeToStageDir)

  override def toString = {
    val lines = instructions.map(_.toInstructionString)
    lines.mkString("\n")
  }
}

trait DockerfileApi {

  import Instructions._

  def addInstruction(instruction: Instruction)

  def copyToStageDir(source: File, targetRelativeToStageDir: File)

  def from(imageName: String) = addInstruction(From(imageName))

  def maintainer(name: String) = addInstruction(Maintainer(name))

  def maintainer(name: String, email: String) = addInstruction(Maintainer(s"$name <$email>"))

  def run(command: String) = addInstruction(Run(command))

  def run(args: String*) = addInstruction(Run(args: _*))

  def cmd(args: String*) = addInstruction(Cmd(args: _*))

  def expose(port: Int) = addInstruction(Expose(port))

  def env(key: String, value: String) = addInstruction(Env(key, value))

  /**
   * Creates a [[sbtdocker.Instructions.Add]] instruction.
   * @param from Path to copy from, relative to the staging dir.
   * @param to Path to copy to inside the container.
   */
  def add(from: String, to: String) = addInstruction(Add(from, to))

  /**
   * Creates a [[sbtdocker.Instructions.Add]] instruction.
   * @param from Path to copy from, relative to the staging dir.
   * @param to Path to copy to inside the container.
   */
  def add(from: Path, to: Path) = addInstruction(Add(from.toString, to.toString))

  /**
   * Creates a [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory if it is not already in it (requires `stageDir` to be set).
   * @param from Path on the local file system to a file or directory.
   * @param to Path to copy to inside the container.
   */
  def add(from: File, to: File)(implicit stageDir: File = file("/")) {
    val fromPath = IO.relativize(stageDir, from).getOrElse {
      val fromName = from.getName
      copyToStageDir(from, file(fromName))
      fromName
    }
    addInstruction(Add(fromPath, to.getPath))
  }

  def entryPoint(args: String*) = addInstruction(EntryPoint(args: _*))

  def volume(mountPoint: String) = addInstruction(Volume(mountPoint))

  def user(username: String) = addInstruction(User(username))

  def workDir(path: String) = addInstruction(WorkDir(path))
}

object Instructions {

  def escapeQuotationMarks(str: String) = str.replace("\"", "\\\"")

  trait Instruction {
    self: Product =>
    def arguments = productIterator.mkString(" ")

    def toInstructionString = {
      val instructionName = productPrefix.toUpperCase
      s"$instructionName $arguments"
    }
  }

  trait SeqArguments {
    self: Instruction =>
    def args: Seq[String]

    override def arguments = args map escapeQuotationMarks mkString("[\"", "\", \"", "\"]")
  }

  case class From(from: String) extends Instruction

  case class Maintainer(name: String) extends Instruction

  object Run {
    def apply(args: String*): Run = Run(args.map(escapeQuotationMarks).mkString(" "))
  }

  case class Run(command: String) extends Instruction

  case class Cmd(args: String*) extends Instruction with SeqArguments

  case class Expose(port: Int) extends Instruction

  case class Env(key: String, value: String) extends Instruction

  case class Add(from: String, to: String) extends Instruction

  case class EntryPoint(args: String*) extends Instruction with SeqArguments

  case class Volume(mountPoint: String) extends Instruction

  case class User(username: String) extends Instruction

  case class WorkDir(path: String) extends Instruction

}