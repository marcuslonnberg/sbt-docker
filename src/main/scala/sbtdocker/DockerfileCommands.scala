package sbtdocker

import sbt._
import sbtdocker.Instructions._
import sbtdocker.staging.{CopyFile, SourceFile}

trait DockerfileLike extends DockerfileCommands {
  type T <: DockerfileLike

  def instructions: Seq[Instruction]
}

trait DockerfileCommands {
  type T <: DockerfileCommands

  def addInstruction(instruction: Instruction): T

  def addInstructions(instructions: TraversableOnce[Instruction]): T

  protected def self: T

  @deprecated("Use stageFile instead.", "0.4.0")
  def copyToStageDir(source: File, targetRelativeToStageDir: File): T = stageFile(source, targetRelativeToStageDir)

  /**
   * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
   *
   * The `target` file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
   * files on the same target.
   *
   * @param source File to copy into stage dir.
   * @param target Path to copy file to, should be relative to the stage dir.
   */
  def stageFile(source: File, target: File): T = {
    addInstruction(Instructions.StageFiles(CopyFile(source), target.getPath))
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
    addInstruction(Instructions.StageFiles(CopyFile(source), target))
  }

  /**
   * Stages a multiple files.
   *
   * @param sources What to stage.
   * @param target Destination directory in the staging directory.
   */
  def stageFiles(sources: Seq[File], target: String): T = {
    addInstruction(Instructions.StageFiles(sources.map(CopyFile), target))
  }

  /**
   * Stages a single source.
   *
   * @param source What to stage.
   * @param target Destination path in the staging directory.
   */
  def stageFile(source: SourceFile, target: String): T = addInstruction(StageFiles(source, target))

  // Instructions

  def from(image: String): T = addInstruction(From(image))

  def from(image: ImageName): T = addInstruction(From(image.toString))

  def label(labelName: String, labelValue: String): T = addInstruction(Label(labelName, labelValue))

  def label(lables: (String, String)*) = {
    if (lables.nonEmpty) addInstruction(Label(lables.toMap))
    else self
  }

  def label(lables: Map[String, String]) = addInstruction(Label(lables))

  def labelRaw(labels: String): T = addInstruction(Label(labels))

  def maintainer(name: String): T = addInstruction(Maintainer(name))

  def maintainer(name: String, email: String): T = addInstruction(Maintainer(s"$name <$email>"))

  /**
   * Execute a command in the image.
   * Uses exec form. Which means the command will not be executed in a shell.
   *
   * Example:
   * {{{
   *   run("executable", "parameter1", "parameter 2")
   * }}}
   * this will yield the raw instruction `RUN ["executable", "parameter1", "parameter 2"]`.
   *
   * @param args An executable followed by eventual parameters.
   */
  def run(args: String*): T = {
    if (args.nonEmpty) addInstruction(Instructions.Run.exec(args))
    else self
  }

  /**
   * Execute a command in the image.
   * The command will be executed through a shell (`/bin/sh`).
   *
   * Example:
   * {{{
   *   runShell("executable", "parameter1", "parameter 2")
   * }}}
   * this will yield the raw instruction `RUN executable parameter1 parameter\ 2`.
   *
   * @param args A command followed by eventual parameters.
   */
  def runShell(args: String*): T = {
    if (args.nonEmpty) addInstruction(Instructions.Run.shell(args))
    else self
  }

  /**
   * Execute a command in the image.
   *
   * Example:
   * {{{
   *   runRaw("executable parameter1 parameter 2")
   * }}}
   * this will yield the raw instruction `RUN executable parameter1 parameter 2`.
   *
   * @param command A command including parameters (on shell or exec form).
   */
  def runRaw(command: String): T = addInstruction(Instructions.Run(command))

  def cmd(args: String*): T = {
    if (args.nonEmpty) addInstruction(Cmd.exec(args))
    else self
  }

  def cmdShell(args: String*): T = {
    if (args.nonEmpty) addInstruction(Cmd.shell(args))
    else self
  }

  def cmdRaw(command: String): T = addInstruction(Cmd(command))

  def expose(ports: Int*): T = {
    if (ports.nonEmpty) addInstruction(Expose(ports))
    else self
  }

  def exposeUdp(ports: Int*): T = {
    if (ports.nonEmpty) addInstruction(ExposeUdp(ports))
    else self
  }

  def env(key: String, value: String): T = addInstruction(Env(key, value))

  def env(variables: (String, String)*) = {
    if (variables.nonEmpty) addInstruction(Env(variables.toMap))
    else self
  }

  def env(variables: Map[String, String]) = addInstruction(Env(variables))

  def envRaw(variables: String): T = addInstruction(Env(variables))

  def add(source: File, destination: String): T = addInstruction(Add(CopyFile(source), destination))

  def add(sources: Seq[File], destination: String): T = addInstruction(Add(sources.map(CopyFile), destination))

  def add(source: File, destination: File): T = addInstruction(Add(CopyFile(source), destination.getPath))

  @deprecated("Use addRaw instead.", "1.0.0")
  def add(source: URL, destination: String): T = addRaw(source, destination)

  @deprecated("Use addRaw instead.", "1.0.0")
  def add(source: URL, destination: File): T = addRaw(source, destination)

  @deprecated("Use addRaw instead.", "1.0.0")
  def add(source: String, destination: String): T = addRaw(source, destination)

  @deprecated("Use addRaw instead.", "1.0.0")
  def add(source: String, destination: File): T = addRaw(source, destination)

  def addRaw(source: URL, destination: String): T = addInstruction(AddRaw(source.toString, destination))

  def addRaw(source: URL, destination: File): T = addInstruction(AddRaw(source.toString, destination.toString))

  def addRaw(source: String, destination: String): T = addInstruction(AddRaw(source, destination))

  def addRaw(source: String, destination: File): T = addInstruction(AddRaw(source, destination.toString))

  def copy(source: File, destination: String): T = addInstruction(Copy(CopyFile(source), destination))

  def copy(sources: Seq[File], destination: String): T = addInstruction(Copy(sources.map(CopyFile), destination))

  def copy(source: File, destination: File): T = addInstruction(Copy(CopyFile(source), destination.toString))

  @deprecated("Use copyRaw instead.", "1.0.0")
  def copy(source: URL, destination: String): T = copyRaw(source, destination)

  @deprecated("Use copyRaw instead.", "1.0.0")
  def copy(source: URL, destination: File): T = copyRaw(source, destination)

  @deprecated("Use copyRaw instead.", "1.0.0")
  def copy(source: String, destination: String): T = copyRaw(source, destination)

  @deprecated("Use copyRaw instead.", "1.0.0")
  def copy(source: String, destination: File): T = copyRaw(source, destination)

  @deprecated("Invalid instruction, use addRaw instead", "1.0.1")
  def copyRaw(source: URL, destination: String): T = addInstruction(CopyRaw(source.toString, destination))

  @deprecated("Invalid instruction, use addRaw instead", "1.0.1")
  def copyRaw(source: URL, destination: File): T = addInstruction(CopyRaw(source.toString, destination.toString))

  def copyRaw(source: String, destination: String): T = addInstruction(CopyRaw(source, destination))

  def copyRaw(source: String, destination: File): T = addInstruction(CopyRaw(source, destination.toString))

  def entryPoint(args: String*): T = {
    if (args.nonEmpty) addInstruction(EntryPoint.exec(args))
    else self
  }

  def entryPointShell(args: String*): T = {
    if (args.nonEmpty) addInstruction(EntryPoint.shell(args))
    else self
  }

  def entryPointRaw(command: String): T = {
    if (command.nonEmpty) addInstruction(EntryPoint(command))
    else self
  }

  def volume(mountPoints: String*): T = {
    if (mountPoints.nonEmpty) addInstruction(Volume(mountPoints))
    else self
  }

  def user(username: String): T = addInstruction(User(username))

  def workDir(path: String): T = addInstruction(WorkDir(path))

  def onBuild(instruction: DockerfileInstruction): T = addInstruction(Instructions.OnBuild(instruction))

}
