package sbtdocker.immutable

import sbtdocker.Dockerfile.CopyPath
import sbtdocker.{Instruction, ImageName, Instructions}
import sbt._

import java.io.File

import sbtdocker.Instructions._

object Dockerfile {
  def empty = Dockerfile()
}

case class Dockerfile(instructions: Seq[Instruction] = Seq.empty,
                      stagedFiles: Seq[CopyPath] = Seq.empty) extends DockerfileCommands {
  type T = Dockerfile

  def mkString = instructions.mkString("\n")

  def addInstruction(instruction: Instruction) = Dockerfile(instructions :+ instruction, stagedFiles)

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * The destination file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
   * files on the same destination.
   *
   * @param source File to copy into stage dir.
   * @param destination Path to copy file to, should be relative to the stage dir.
   */
  def stageFile(source: File, destination: File) = {
    val copy = CopyPath(source, destination)
    Dockerfile(instructions, stagedFiles :+ copy)
  }

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * If the destination ends with a "/" then the source filename will be added at the end.
   *
   * The destination file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
   * files on the same destination.
   *
   * @param source File to copy into stage dir.
   * @param destination Path to copy file to, should be relative to the stage dir.
   */
  def stageFile(source: File, destination: String) = {
    val targetFile = expandPath(source, destination)
    val copy = CopyPath(source, targetFile)
    Dockerfile(instructions, stagedFiles :+ copy)
  }

  private def expandPath(source: File, path: String) = {
    val pathFile = file(path)
    if (path.endsWith("/")) pathFile / source.name
    else pathFile
  }
}

trait DockerfileCommands {
  type T <: DockerfileCommands

  def addInstruction(instruction: Instruction): T

  def stageFile(from: File, target: File): T

  def stageFile(from: File, target: String): T

  // Instructions

  def from(image: String) = addInstruction(Instructions.From(image))

  def from(image: ImageName) = addInstruction(Instructions.From(image.toString))

  def maintainer(name: String) = addInstruction(Instructions.Maintainer(name))

  def maintainer(name: String, email: String) = addInstruction(Instructions.Maintainer(s"$name <$email>"))

  def run(args: String*) = addInstruction(Instructions.Run(args: _*))

  def runShell(args: String*) = addInstruction(Instructions.Run.shell(args: _*))

  def cmd(args: String*) = addInstruction(Cmd(args: _*))

  def cmdShell(args: String*) = addInstruction(Cmd.shell(args: _*))

  def expose(ports: Int*) = addInstruction(Expose(ports: _*))

  def env(key: String, value: String) = addInstruction(Env(key, value))

  def add(source: File, destination: String) = {
    addInstruction(Add(source.toString, destination))
      .stageFile(source, destination)
  }

  def add(source: File, destination: File) = {
    addInstruction(Add(source.toString, destination.toString))
      .stageFile(source, destination)
  }

  def add(source: URL, destination: String) = addInstruction(Add(source.toString, destination))

  def add(source: URL, destination: File) = addInstruction(Add(source.toString, destination.toString))

  def copy(source: File, destination: String) = {
    addInstruction(Copy(source.toString, destination))
      .stageFile(source, destination)
  }

  def copy(source: File, destination: File) = {
    addInstruction(Copy(source.toString, destination.toString))
      .stageFile(source, destination)
  }

  def copy(source: URL, destination: String) = addInstruction(Copy(source.toString, destination))

  def copy(source: URL, destination: File) = addInstruction(Copy(source.toString, destination.toString))

  def entryPoint(args: String*) = addInstruction(EntryPoint(args: _*))

  def entryPointShell(args: String*) = addInstruction(EntryPoint.shell(args: _*))

  def volume(mountPoint: String) = addInstruction(Volume(mountPoint))

  def user(username: String) = addInstruction(User(username))

  def workDir(path: String) = addInstruction(WorkDir(path))

  def onBuild(instruction: Instruction) = addInstruction(OnBuild(instruction))

}
