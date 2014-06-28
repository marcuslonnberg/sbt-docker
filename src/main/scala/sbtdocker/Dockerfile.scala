package sbtdocker

import sbt._
import Dockerfile._
import Instructions._

object Dockerfile {

  case class CopyPath(source: File, targetRelative: File)

}

/**
 * Mutable Dockerfile. Contains instructions and paths that should be copied to the staging directory.
 *
 * @param instructions Sequence of ordered instructions
 * @param pathsToCopy Paths that should be copied to the staging directory
 */
case class Dockerfile(var instructions: Seq[Instruction] = Seq.empty,
                      var pathsToCopy: Seq[Dockerfile.CopyPath] = Seq.empty) extends DockerfileCommands {
  def addInstruction(instruction: Instruction) = instructions :+= instruction

  def copyToStageDir(source: File, targetRelativeToStageDir: File) = pathsToCopy :+= CopyPath(source, targetRelativeToStageDir)

  def toInstructionsString = {
    val lines = instructions.map(_.toString)
    lines.mkString("\n")
  }

  /**
   * Add an [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  override def add(from: File, to: File) = {
    val target = file(expandPath(from, to))
    // If there is already a queued copy with the same destination path but with a different source file.
    // Then set a different name on the file while its in the staging directory.
    if (collidingCopy(from, target)) {
      val stagePath = target.getPath + from.hashCode()
      copyToStageDir(from, file(stagePath))
      addInstruction(Add(stagePath, target.getPath))
    } else {
      super.add(from, to)
    }
  }

  /**
   * Add an [[sbtdocker.Instructions.Copy]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  override def copy(from: File, to: File) = {
    val target = file(expandPath(from, to))
    // If there is already a queued copy with the same destination path but with a different source file.
    // Then set a different name on the file while its in the staging directory.
    if (collidingCopy(from, target)) {
      val stagePath = target.getPath + from.hashCode()
      copyToStageDir(from, file(stagePath))
      addInstruction(Copy(stagePath, target.getPath))
    } else {
      super.copy(from, to)
    }
  }

  /**
   * Check if there is already a queued copy with the same destination path but with a different source file.
   */
  def collidingCopy(from: File, target: File): Boolean = {
    val sameTarget = pathsToCopy.filter(_.targetRelative == target)
    sameTarget.nonEmpty && sameTarget.exists(_.source != from)
  }
}

trait DockerfileCommands {

  import Instructions._

  def addInstruction(instruction: Instruction)

  def copyToStageDir(source: File, targetRelativeToStageDir: File)

  /**
   * Add a [[sbtdocker.Instructions.From]] instruction.
   */
  def from(imageName: String) = addInstruction(From(imageName))

  /**
   * Add a [[sbtdocker.Instructions.From]] instruction.
   */
  def from(imageName: ImageName) = addInstruction(From(imageName.toString))

  /**
   * Add a [[sbtdocker.Instructions.Maintainer]] instruction.
   */
  def maintainer(name: String) = addInstruction(Maintainer(name))

  /**
   * Add a [[sbtdocker.Instructions.Maintainer]] instruction.
   */
  def maintainer(name: String, email: String) = addInstruction(Maintainer(s"$name <$email>"))

  /**
   * Add a [[sbtdocker.Instructions.Run]] instruction.
   */
  def run(args: String*) = addInstruction(Run(args: _*))

  /**
   * Add a [[sbtdocker.Instructions.Run]] instruction that executes the specified command in a shell.
   */
  def runShell(args: String*) = addInstruction(Run.shell(args: _*))

  /**
   * Add a [[sbtdocker.Instructions.Cmd]] instruction.
   */
  def cmd(args: String*) = addInstruction(Cmd(args: _*))

  /**
   * Add a [[sbtdocker.Instructions.Cmd]] instruction that executes the specified command in a shell.
   */
  def cmdShell(args: String*) = addInstruction(Cmd.shell(args: _*))

  /**
   * Add an [[sbtdocker.Instructions.Expose]] instruction.
   */
  def expose(ports: Int*) = addInstruction(Expose(ports: _*))

  /**
   * Add a [[sbtdocker.Instructions.Env]] instruction.
   */
  def env(key: String, value: String) = addInstruction(Env(key, value))

  /**
   * Add an [[sbtdocker.Instructions.Add]] instruction.
   * @param from Path to copy from where the stage directory is the root.
   * @param to Path to copy to inside the container.
   */
  def add(from: String, to: String) = addInstruction(Add(from, to))

  /**
   * Add an [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  def add(from: File, to: String): Unit = add(from, file(to))

  /**
   * Add an [[sbtdocker.Instructions.Add]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  def add(from: File, to: File): Unit = {
    val toPathString = expandPath(from, to)
    copyToStageDir(from, file(toPathString))
    addInstruction(Add(toPathString, toPathString))
  }

  /**
   * Add a [[sbtdocker.Instructions.Copy]] instruction.
   * @param from Path to copy from where the stage directory is the root.
   * @param to Path to copy to inside the container.
   */
  def copy(from: String, to: String) = addInstruction(Copy(from, to))

  /**
   * Add a [[sbtdocker.Instructions.Copy]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  def copy(from: File, to: String): Unit = copy(from, file(to))

  /**
   * Add a [[sbtdocker.Instructions.Copy]] instruction.
   * Also copies the `from` path into the staging directory.
   * @param from File or directory on the local file system.
   * @param to Path to copy to inside the container.
   */
  def copy(from: File, to: File): Unit = {
    val toPathString = expandPath(from, to)
    copyToStageDir(from, file(toPathString))
    addInstruction(Copy(toPathString, toPathString))
  }

  /**
   * If the `to` path ends with a '/' then append the name of the `from` file.
   */
  protected def expandPath(from: File, to: File): String = {
    if (to.getPath.endsWith("/")) (to / from.name).getPath
    else to.toString
  }

  /**
   * Add a [[sbtdocker.Instructions.EntryPoint]] instruction.
   */
  def entryPoint(args: String*) = addInstruction(EntryPoint(args: _*))

  /**
   * Add a [[sbtdocker.Instructions.EntryPoint]] instruction that executes the specified command in a shell.
   */
  def entryPointShell(args: String*) = addInstruction(EntryPoint.shell(args: _*))

  /**
   * Add a [[sbtdocker.Instructions.Volume]] instruction.
   */
  def volume(mountPoint: String) = addInstruction(Volume(mountPoint))

  /**
   * Add a [[sbtdocker.Instructions.User]] instruction.
   */
  def user(username: String) = addInstruction(User(username))

  /**
   * Add a [[sbtdocker.Instructions.WorkDir]] instruction.
   */
  def workDir(path: String) = addInstruction(WorkDir(path))

  /**
   * Add a [[sbtdocker.Instructions.OnBuild]] instruction.
   */
  def onBuild(instruction: Instruction) = addInstruction(OnBuild(instruction))
}
