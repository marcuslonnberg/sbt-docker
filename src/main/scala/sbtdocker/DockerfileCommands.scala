package sbtdocker

import sbt._
import sbtdocker.Instructions._
import Utils._

object CopyPath {
  def apply(source: File, destination: String): CopyPath = {
    val destinationFile = expandPath(source, destination)
    CopyPath(source, destinationFile)
  }
}

case class CopyPath(source: File, destination: File)

trait DockerfileLike[T <: DockerfileLike[T]] extends DockerfileCommands[T] {
  this: T =>

  def instructions: Seq[Instruction]

  def stagedFiles: Seq[CopyPath]

  def mkString = instructions.mkString("\n")
}

trait DockerfileCommands[T <: DockerfileCommands[T]] {
  this: T =>

  def addInstruction(instruction: Instruction): T

  @deprecated("Use stageFile instead.", "0.4.0")
  def copyToStageDir(source: File, destination: File) = stageFile(source, destination)

  @deprecated("Use stageFile instead.", "0.4.0")
  def copyToStageDir(source: File, destination: String) = stageFile(source, destination)

  def stageFile(file: CopyPath): T

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * The destination file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
   * files on the same destination.
   *
   * @param source File to copy into stage dir.
   * @param destination Path to copy file to, should be relative to the stage dir.
   */
  def stageFile(source: File, destination: File): T = {
    val copy = CopyPath(source, destination)
    stageFile(copy)
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
  def stageFile(source: File, destination: String): T = {
    val copy = CopyPath(source, destination)
    stageFile(copy)
  }

  def stageFiles(files: TraversableOnce[CopyPath]): T

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
    addInstruction(Add(expandPath(source, destination).toString, destination))
      .stageFile(source, destination)
  }

  def add(source: File, destination: File) = {
    addInstruction(Add(destination.toString, destination.toString))
      .stageFile(source, destination)
  }

  def add(source: URL, destination: String) = addInstruction(Add(source.toString, destination))

  def add(source: URL, destination: File) = addInstruction(Add(source.toString, destination.toString))

  def add(source: String, destination: String) = addInstruction(Add(source, destination))

  def add(source: String, destination: File) = addInstruction(Add(source, destination.toString))

  def copy(source: File, destination: String) = {
    addInstruction(Copy(expandPath(source, destination).toString, destination))
      .stageFile(source, destination)
  }

  def copy(source: File, destination: File) = {
    addInstruction(Copy(destination.toString, destination.toString))
      .stageFile(source, destination)
  }

  def copy(source: URL, destination: String) = addInstruction(Copy(source.toString, destination))

  def copy(source: URL, destination: File) = addInstruction(Copy(source.toString, destination.toString))

  def copy(source: String, destination: String) = addInstruction(Copy(source, destination))

  def copy(source: String, destination: File) = addInstruction(Copy(source, destination.toString))

  def entryPoint(args: String*) = addInstruction(EntryPoint(args: _*))

  def entryPointShell(args: String*) = addInstruction(EntryPoint.shell(args: _*))

  def volume(mountPoint: String) = addInstruction(Volume(mountPoint))

  def user(username: String) = addInstruction(User(username))

  def workDir(path: String) = addInstruction(WorkDir(path))

  def onBuild(instruction: Instruction) = addInstruction(OnBuild(instruction))

}
