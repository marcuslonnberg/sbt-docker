package sbtdocker.immutable

import sbtdocker.{CopyPath, DockerfileLike, Instruction}

object Dockerfile {
  def empty = Dockerfile()
}

/**
 * Immutable Dockerfile.
 *
 * @example {{{
 *  val jarFile: File
 *
 *  Dockerfile.empty
 *    .from("dockerfile/java")
 *    .add(jarFile, "/srv/app.jar")
 *    .workDir("/srv")
 *    .cmd("java", "-jar", "app.jar")
 *  }}}
 *
 * @param instructions Ordered sequence of Dockerfile instructions.
 * @param stagedFiles Files and directories that should be copied to the stage directory.
 */
case class Dockerfile(instructions: Seq[Instruction] = Seq.empty,
                      stagedFiles: Seq[CopyPath] = Seq.empty) extends DockerfileLike[Dockerfile] {

  def addInstruction(instruction: Instruction) = Dockerfile(instructions :+ instruction, stagedFiles)

  def stageFile(file: CopyPath) = Dockerfile(instructions, stagedFiles :+ file)

  def stageFiles(files: TraversableOnce[CopyPath]) = Dockerfile(instructions, stagedFiles ++ files)
}
