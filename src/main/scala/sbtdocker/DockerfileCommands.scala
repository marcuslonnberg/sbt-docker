package sbtdocker

import java.io.File

import sbt._
import sbtdocker.Instructions._
import sbtdocker.Utils._

object StageFile {
  /**
   * @param source File that should be staged.
   * @param target Target path in the stage directory.
   *               If it ends with "/" then it will be expanded with the filename of the `source`.
   */
  def apply(source: File, target: String): StageFile = {
    val destinationFile = expandPath(source, target)
    StageFile(source, destinationFile)
  }
}

/**
 * Container for a file (or directory) that should be copied to a path in the stage directory.
 *
 * @param source File that should be staged.
 * @param target Path in the stage directory.
 */
case class StageFile(source: File, target: File)

/**
 * Container for a directory that should be copied to a path in the stage directory in the form
 * of a tar.gz file.
 *
 * @param file The path that should be compressed.
 * @param dest Path in the stage directory.
 */
case class StagedArchive(file: File, dest: String) {
  val tempFile = java.io.File.createTempFile("dockerbuild", ".tgz")

  // We lazily build the actual tar ball, only when the SBT caching detects that the contents have
  // changed.
  lazy val outputFile = {
    val parentDir = file.getParent
    val command = s"tar czvf $tempFile -C $parentDir ${file.getName}"
    println(command)
    command.!!

    StageFile(tempFile, expandPath(tempFile, "/"))
  }
}

trait DockerfileLike extends DockerfileCommands {
  type T <: DockerfileLike

  def instructions: Seq[Instruction]

  def stagedFiles: Seq[StageFile]

  def stagedArchives: Seq[StagedArchive]

  def mkString = instructions.mkString("\n")
}

trait DockerfileCommands {
  type T <: DockerfileCommands

  def addInstruction(instruction: Instruction): T

  @deprecated("Use stageFile instead.", "0.4.0")
  def copyToStageDir(source: File, targetRelativeToStageDir: File) = stageFile(source, targetRelativeToStageDir)

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * The target file must be unique for this Dockerfile. Otherwise files that are staged later with the same target
   * will overwrite this file.
   */
  def stageFile(file: StageFile): T

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * The `target` file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
   * files on the same target.
   *
   *@param source File to copy into stage dir.
   * @param target Path to copy file to, should be relative to the stage dir.
   */
  def stageFile(source: File, target: File): T = {
    val copy = StageFile(source, target)
    stageFile(copy)
  }

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * If the `target` ends with / then the source filename will be added at the end.
   *
   * The `target` file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
   * files on the same target.
   *
   * @param source File to copy into stage dir.
   * @param target Path to copy file to, should be relative to the stage dir.
   */
  def stageFile(source: File, target: String): T = {
    val copy = StageFile(source, target)
    stageFile(copy)
  }

  /**
   * Adds an directory that will be transmitted to the docker daemon in the form of a tar.gz file.
   * This format is convenient when you want to preserve symlinks.
   * @param archive
   */
  def stageArchive(archive: StagedArchive): T


  /**
   * Stage multiple files.
   */
  def stageFiles(files: TraversableOnce[StageFile]): T

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

  def addCompressed(file: File, dest: String): T = {
    val archive = StagedArchive(file, dest)
    stageArchive(archive)
    addInstruction(Add(archive.tempFile.getName, dest))
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
