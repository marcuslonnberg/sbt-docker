package sbtdocker

import org.apache.commons.lang3.StringEscapeUtils

trait Instruction

trait DockerInstruction extends Instruction {
  def arguments: String

  def instructionName: String

  override def toString = s"$instructionName $arguments"

  @deprecated("Use toString instead.", "0.4.0")
  def toInstructionString = toString
}

trait ProductDockerInstruction extends DockerInstruction {
  this: Product =>
  def arguments = productIterator.mkString(" ")

  def instructionName = productPrefix.toUpperCase
}

trait FileInstruction extends Instruction {
  require(sources.nonEmpty, "Must have at least one source path")
  require(sources.length == 1 || destination.endsWith("/"),
    "When multiple source files are specified the destination must end with a slash \"/\".")

  def sources: Seq[SourceFile]

  def destination: String

  def dockerInstruction(sources: Seq[String], destination: String): Option[DockerInstruction]
}

trait StagedFileInstruction extends FileInstruction

object Instructions {

  import InstructionUtils._

  object From {
    def apply(image: ImageName): From = new From(image.toString)
  }

  /**
   * Sets the base image for the image.
   * @param image Name of the image.
   */
  case class From(image: String) extends ProductDockerInstruction

  /**
   * Author of the image.
   * @param name Author name.
   */
  case class Maintainer(name: String) extends ProductDockerInstruction

  object Run {
    /**
     * Executes a command directly, without using a shell which means that variables will not be substituted.
     * @param commands Command list.
     */
    def exec(commands: Seq[String]) = Run(jsonArrayString(commands))

    /**
     * Executes a command through a shell (`/bin/sh`).
     * @param commands Command.
     */
    def shell(commands: Seq[String]) = Run(shellCommandString(commands))
  }

  /**
   * Executes a command when building the image.
   * @param command Command which should be executed.
   */
  case class Run(command: String) extends ProductDockerInstruction

  object EntryPoint {
    /**
     * Command to execute when the image starts.
     * @param commands Command list.
     */
    def exec(commands: Seq[String]) = EntryPoint(jsonArrayString(commands))

    /**
     * Command to execute through a shell (`/bin/sh`) when the image starts.
     * @param commands Command list.
     */
    def shell(commands: Seq[String]) = EntryPoint(shellCommandString(commands))
  }

  /**
   * Command to execute when the image starts.
   * @param command Command.
   */
  case class EntryPoint(command: String) extends ProductDockerInstruction

  object Cmd {
    /**
     * Command to execute when the image starts, or default arguments to the entry point.
     * @param commands Command list.
     */
    def exec(commands: Seq[String]) = Cmd(jsonArrayString(commands))

    /**
     * Command to execute through a shell (`/bin/sh`) when the image starts, or default arguments
     * to the entry point.
     * @param commands Command list.
     */
    def shell(commands: Seq[String]) = Cmd(shellCommandString(commands))
  }

  /**
   * When an entry point is specified `CMD` acts as default arguments that is appended the the
   * entry point command.
   * If an entry point is not specified `CMD` will be the default command for the image.
   * @param command Command.
   */
  case class Cmd(command: String) extends ProductDockerInstruction

  /**
   * Exposes network ports at runtime.
   * @param ports Port numbers to expose.
   */
  case class Expose(ports: Seq[Int]) extends ProductDockerInstruction {
    require(ports.nonEmpty, "Must expose at least one port")

    override def arguments = ports.mkString(" ")
  }

  object Env {
    def apply(variables: Map[String, String]): Env = {
      Env(variables.map { case (key, value) => formatKeyValue(key, value)}.mkString(" "))
    }

    def apply(key: String, value: String): Env = {
      Env(formatKeyValue(key, value))
    }

    def formatKeyValue(key: String, value: String): String = {
      escapeEnvironmentVariable(key) + "=" + escapeEnvironmentVariable(value)
    }
  }

  /**
   * Sets environment variables in the image.
   * Example: "A=1 B=2 C=3"
   * @param variables Environment variables.
   */
  case class Env(variables: String) extends ProductDockerInstruction

  object Add {
    def apply(source: SourceFile, destination: String): Add = Add(Seq(source), destination)
  }
  
  /**
   * Adds files to the image.
   * If a source file is a tar file it will be extracted as a directory.
   * @param sources Source files.
   * @param destination Destination path inside the container.
   */
  case class Add(sources: Seq[SourceFile], destination: String) extends FileInstruction {
    def dockerInstruction(sources: Seq[String], destination: String) = Some(AddRaw(sources, destination))
  }

  object AddRaw {
    def apply(source: String, destination: String): AddRaw = AddRaw(Seq(source), destination)
  }

  /**
   * Adds files from the staging directory to the image.
   * If a source file is a tar file it will be extracted as a directory.
   * @param sources Source path inside the staging directory.
   * @param destination Destination path inside the container.
   */
  case class AddRaw(sources: Seq[String], destination: String) extends ProductDockerInstruction {
    override def instructionName = "ADD"

    override def arguments = sources.mkString(" ") + " " + destination
  }

  object Copy {
    def apply(source: SourceFile, destination: String): Copy = Copy(Seq(source), destination)
  }

  /**
   * Adds files to the image.
   * @param sources Source files. Cannot be empty.
   * @param destination Destination path inside the container.
   */
  case class Copy(sources: Seq[SourceFile], destination: String) extends FileInstruction {
    def dockerInstruction(sources: Seq[String], destination: String) = Some(CopyRaw(sources, destination))
  }

  object CopyRaw {
    def apply(source: String, destination: String): CopyRaw = CopyRaw(Seq(source), destination)
  }

  /**
   * Adds files from the staging directory to the image.
   * @param sources Source path inside the staging directory. Cannot be empty.
   * @param destination Destination path inside the container.
   */
  case class CopyRaw(sources: Seq[String], destination: String) extends ProductDockerInstruction {
    override def instructionName = "COPY"

    override def arguments = sources.mkString(" ") + " " + destination
  }

  object Volume {
    def apply(path: String): Volume = Volume(Seq(path))
  }

  /**
   * Add mount points inside the container.
   * @param paths Paths inside the container. Cannot be empty.
   */
  case class Volume(paths: Seq[String]) extends ProductDockerInstruction {
    require(paths.nonEmpty, "Must have at least one volume path")

    override def arguments = jsonArrayString(paths)
  }

  /**
   * Sets the user inside the container that should be used for the next instructions.
   * @param username Name of the user.
   */
  case class User(username: String) extends ProductDockerInstruction

  /**
   * Sets the current working directory for the instructions that follow.
   * @param path Path inside the container.
   */
  case class WorkDir(path: String) extends ProductDockerInstruction

  /**
   * Adds a trigger of a instruction that will be run when the image is used as a base image (`FROM`).
   * @param instruction Instruction to run.
   */
  case class OnBuild(instruction: DockerInstruction) extends ProductDockerInstruction

  object StageFile {
    def apply(source: SourceFile, destination: String): StageFile = StageFile(Seq(source), destination)
  }

  case class StageFile(sources: Seq[SourceFile], destination: String) extends StagedFileInstruction {
    def dockerInstruction(sources: Seq[String], destination: String) = None
  }

  /**
   * This class allows the user to specify a raw Dockerfile instruction.
   * Example:
   * {{{
   * Raw("RUN", "echo '123'")
   * // Will yield the following string in the Dockerfile:
   * "RUN echo '123'"
   * }}}
   * @param instructionName Name of the instruction.
   * @param arguments Argument string.
   */
  case class Raw(instructionName: String, arguments: String) extends DockerInstruction

}

private[sbtdocker] object InstructionUtils {
  def escapeEnvironmentVariable(value: String) = {
    value
      .replace(" ", "\\ ")
      .replaceAll("\\t", "\\\\t")
      .replaceAll("\\n", "\\\\n")
      .replace("=", "\\\\=")
  }

  def shellCommandString(commands: Seq[String]) = {
    def escape(str: String) = {
      str.replace(" ", "\\ ")
        .replaceAll("\\t", "\\\\t")
        .replaceAll("\\n", "\\\\n")
        .replace("\"", "\\\"")
    }

    commands.map(escape).mkString(" ")
  }

  def jsonArrayString(args: Seq[String]) = args.map(StringEscapeUtils.escapeJson).mkString("[\"", "\", \"", "\"]")
}
