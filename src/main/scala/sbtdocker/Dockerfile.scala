package sbtdocker

import sbt._
import java.nio.file.{Paths, Path}

object Dockerfile {

  case class CopyPath(source: File, targetRelative: File)

  case class StageDir(file: File)

}

import Dockerfile._
import Instructions._

/**
 * Mutable Dockerfile. Contains instructions and paths that should be copied to the staging directory.
 *
 * @param instructions Sequence of ordered instructions
 * @param pathsToCopy Paths that should be copied to the staging directory
 */
case class Dockerfile(var instructions: Seq[Instructions.Instruction] = Seq.empty,
                      var pathsToCopy: Seq[Dockerfile.CopyPath] = Seq.empty) extends DockerfileCommands {
  def addInstruction(instruction: Instruction) = instructions :+= instruction

  def copyToStageDir(source: File, targetRelativeToStageDir: File) = pathsToCopy :+= CopyPath(source, targetRelativeToStageDir)

  def toInstructionsString = {
    val lines = instructions.map(_.toInstructionString)
    lines.mkString("\n")
  }

  /**
   * Adds an [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  override def add(from: File, to: Path) = {
    val target = file(expandPath(from, to))
    val sameTarget = pathsToCopy.filter(_.targetRelative == target)
    // If there is already a queued copy with the same destination path but with a different source file.
    // Then we set a different name on the file while its in the staging directory.
    if (sameTarget.nonEmpty && sameTarget.exists(_.source != from)) {
      val stagePath = target.getPath + from.hashCode()
      copyToStageDir(from, file(stagePath))
      addInstruction(Add(stagePath, target.getPath))
    } else {
      super.add(from, to)
    }
  }
}

trait DockerfileCommands {

  import Instructions._

  def addInstruction(instruction: Instruction)

  def copyToStageDir(source: File, targetRelativeToStageDir: File)

  def from(imageName: String) = addInstruction(From(imageName))

  def maintainer(name: String) = addInstruction(Maintainer(name))

  def maintainer(name: String, email: String) = addInstruction(Maintainer(s"$name <$email>"))

  def run(args: String*) = addInstruction(Run(args: _*))

  def cmd(args: String*) = addInstruction(Cmd(args: _*))

  def expose(ports: Int*) = addInstruction(Expose(ports: _*))

  def env(key: String, value: String) = addInstruction(Env(key, value))

  /**
   * Adds an [[sbtdocker.Instructions.Add]] instruction.
   * @param from Path to copy from, relative to the staging dir.
   * @param to Path to copy to inside the container.
   */
  def add(from: String, to: String) = addInstruction(Add(from, to))

  /**
   * Adds an [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  def add(from: File, to: String): Unit = add(from, Paths.get(to))

  /**
   * Adds an [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  def add(from: File, to: Path): Unit = {
    val toPathString = expandPath(from, to)
    copyToStageDir(from, file(toPathString))
    addInstruction(Add(toPathString, toPathString))
  }

  /**
   * If the `to` path ends with a '/' then append the name from the `from` file.
   */
  protected def expandPath(from: File, to: Path): String = {
    if (to.endsWith("/")) (file(to.toString) / from.name).getPath
    else to.toString
  }

  def entryPoint(args: String*) = addInstruction(EntryPoint(args: _*))

  def volume(mountPoint: String) = addInstruction(Volume(mountPoint))

  def user(username: String) = addInstruction(User(username))

  def workDir(path: String) = addInstruction(WorkDir(path))

  def onBuild(instruction: Instruction) = addInstruction(OnBuild(instruction))
}

object Instructions {

  private def escapeQuotationMarks(str: String) = str.replace("\"", "\\\"")

  trait Instruction {
    this: Product =>
    def arguments = productIterator.mkString(" ")

    def instructionName = productPrefix.toUpperCase

    def toInstructionString = {
      s"$instructionName $arguments"
    }
  }

  trait SeqArguments {
    this: Instruction =>
    def args: Seq[String]

    def shellFormat: Boolean

    private def execArguments = args.map(escapeQuotationMarks).mkString("[\"", "\", \"", "\"]")

    private def shellArguments = args.mkString(" ")

    override def arguments = if (shellFormat) shellArguments else execArguments
  }

  case class From(from: String) extends Instruction

  case class Maintainer(name: String) extends Instruction

  object Run {
    def shell(args: String*) = new Run(true, args: _*)

    def apply(args: String*) = new Run(false, args: _*)
  }

  /**
   * RUN instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class Run(shellFormat: Boolean, args: String*) extends Instruction with SeqArguments

  object Cmd {
    def shell(args: String*) = new Cmd(true, args: _*)

    def apply(args: String*) = new Cmd(false, args: _*)
  }

  /**
   * CMD instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class Cmd(shellFormat: Boolean, args: String*) extends Instruction with SeqArguments

  case class Expose(ports: Int*) extends Instruction {
    override def arguments = ports.mkString(" ")
  }

  case class Env(key: String, value: String) extends Instruction

  case class Add(from: String, to: String) extends Instruction

  object EntryPoint {
    def shell(args: String*) = new EntryPoint(true, args: _*)

    def apply(args: String*) = new EntryPoint(false, args: _*)
  }

  /**
   * ENTRYPOINT instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class EntryPoint(shellFormat: Boolean, args: String*) extends Instruction with SeqArguments

  case class Volume(mountPoint: String) extends Instruction

  case class User(username: String) extends Instruction

  case class WorkDir(path: String) extends Instruction

  case class OnBuild(instruction: Instruction) extends Instruction {
    override def arguments = instruction.toInstructionString
  }

}
