package sbtdocker

import sbt._
import sbtdocker.Instructions._

/*object StageFile {
  /**
   * @param source File that should be staged.
   * @param target Target path in the stage directory.
   *               If it ends with "/" then it will be expanded with the filename of the `source`.
   */
  def apply(source: File, target: String): StageFile = {
    val destinationFile = expandPath(source, target)
    StageFile(source, destinationFile)
  }
}*/

/**
 * Container for a file (or directory) that should be copied to a path in the stage directory.
 *
 * @param source File that should be staged.
 * @param target Path in the stage directory.
 */
//case class StageFile(source: File, target: File)

trait DockerfileLike extends DockerfileCommands {
  type T <: DockerfileLike

  def instructions: Seq[Instruction]
}

trait DockerfileCommands {
  type T <: DockerfileCommands

  def addInstruction(instruction: Instruction): T

  def addInstructions(instructions: TraversableOnce[Instruction]): T

  @deprecated("Use stageFile instead.", "0.4.0")
  def copyToStageDir(source: File, targetRelativeToStageDir: File) = stageFile(source, targetRelativeToStageDir)

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * The target file must be unique for this Dockerfile. Otherwise files that are staged later with the same target
   * will overwrite this file.
   */
  def stageFile(file: StageFile): T = {
    addInstruction(file)
  }

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
    stageFile(StageFile(CopyFile(source), target.getPath))
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
    stageFile(StageFile(CopyFile(source), target))
  }

  def stageFiles(sources: Seq[File], target: String): T = {
    addInstruction(StageFile(sources.map(CopyFile), target))
  }

  /**
   * Stage multiple files.
   */
  def stageFiles(files: TraversableOnce[StageFile]): T = {
    addInstructions(files)
  }

  // Instructions

  def from(image: String) = addInstruction(From(image))

  def from(image: ImageName) = addInstruction(From(image.toString))

  def maintainer(name: String) = addInstruction(Maintainer(name))

  def maintainer(name: String, email: String) = addInstruction(Maintainer(s"$name <$email>"))

  def run(args: String*) = addInstruction(Instructions.Run.exec(args))

  def runShell(args: String*) = addInstruction(Instructions.Run.shell(args))

  def runRaw(command: String) = addInstruction(Instructions.Run(command))

  def cmd(args: String*) = addInstruction(Cmd.exec(args))

  def cmdShell(args: String*) = addInstruction(Cmd.shell(args))

  def cmdRaw(command: String) = addInstruction(Cmd(command))

  def expose(ports: Int*) = addInstruction(Expose(ports))

  def env(key: String, value: String) = addInstruction(Env(key, value))

  def add(source: File, destination: String) = addInstruction(Add(CopyFile(source), destination))

  def add(sources: Seq[File], destination: String) = addInstruction(Add(sources.map(CopyFile), destination))

  def add(source: File, destination: File) = addInstruction(Add(CopyFile(source), destination.getPath))

  @deprecated("Use addRaw instead.", "todo")
  def add(source: URL, destination: String) = addRaw(source, destination)

  @deprecated("Use addRaw instead.", "todo")
  def add(source: URL, destination: File) = addRaw(source, destination)

  @deprecated("Use addRaw instead.", "todo")
  def add(source: String, destination: String) = addRaw(source, destination)

  @deprecated("Use addRaw instead.", "todo")
  def add(source: String, destination: File) = addRaw(source, destination)

  def addRaw(source: URL, destination: String) = addInstruction(AddRaw(source.toString, destination))

  def addRaw(source: URL, destination: File) = addInstruction(AddRaw(source.toString, destination.toString))

  def addRaw(source: String, destination: String) = addInstruction(AddRaw(source, destination))

  def addRaw(source: String, destination: File) = addInstruction(AddRaw(source, destination.toString))

  def copy(source: File, destination: String) = addInstruction(Copy(CopyFile(source), destination))
  
  def copy(sources: Seq[File], destination: String) = addInstruction(Copy(sources.map(CopyFile), destination))

  def copy(source: File, destination: File) = addInstruction(Copy(CopyFile(source), destination.toString))

  @deprecated("Use copyRaw instead.", "todo")
  def copy(source: URL, destination: String) = copyRaw(source, destination)

  @deprecated("Use copyRaw instead.", "todo")
  def copy(source: URL, destination: File) = copyRaw(source, destination)

  @deprecated("Use copyRaw instead.", "todo")
  def copy(source: String, destination: String) = copyRaw(source, destination)

  @deprecated("Use copyRaw instead.", "todo")
  def copy(source: String, destination: File) = copyRaw(source, destination)

  def copyRaw(source: URL, destination: String) = addInstruction(CopyRaw(source.toString, destination))

  def copyRaw(source: URL, destination: File) = addInstruction(CopyRaw(source.toString, destination.toString))

  def copyRaw(source: String, destination: String) = addInstruction(CopyRaw(source, destination))

  def copyRaw(source: String, destination: File) = addInstruction(CopyRaw(source, destination.toString))

  def entryPoint(args: String*) = addInstruction(EntryPoint.exec(args))

  def entryPointShell(args: String*) = addInstruction(EntryPoint.shell(args))
  
  def entryPointRaw(command: String) = addInstruction(EntryPoint(command))

  def volume(mountPoints: String*) = addInstruction(Volume(mountPoints))

  def user(username: String) = addInstruction(User(username))

  def workDir(path: String) = addInstruction(WorkDir(path))

  def onBuild(instruction: DockerInstruction) = addInstruction(Instructions.OnBuild(instruction))

}
