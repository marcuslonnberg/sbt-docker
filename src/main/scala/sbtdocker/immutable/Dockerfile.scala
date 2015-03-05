package sbtdocker.immutable

import sbtdocker.{DockerfileLike, Instruction}

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
 * @param instructions Ordered sequence of instructions.
 */
case class Dockerfile(instructions: Seq[Instruction] = Seq.empty) extends DockerfileLike {
  type T = Dockerfile

  def addInstruction(instruction: Instruction) = Dockerfile(instructions :+ instruction)

  def addInstructions(instructions: TraversableOnce[Instruction]) = Dockerfile(this.instructions ++ instructions)

  protected def self = this
}
