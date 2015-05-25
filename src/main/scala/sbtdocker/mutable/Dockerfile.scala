package sbtdocker.mutable

import sbtdocker.{DockerfileLike, Instruction}

/**
 * Mutable Dockerfile.
 *
 * @example {{{
 *  val jarFile: File
 *
 *  new Dockerfile {
 *    from("java")
 *    add(jarFile, "/srv/app.jar")
 *    workDir("/srv")
 *    cmd("java", "-jar", "app.jar")
 *  }
 *  }}}
 *
 * @param instructions Ordered sequence of instructions.
 */
case class Dockerfile(var instructions: Seq[Instruction] = Seq.empty) extends DockerfileLike {
  type T = Dockerfile

  def addInstruction(instruction: Instruction) = {
    instructions :+= instruction
    this
  }

  def addInstructions(instructions: TraversableOnce[Instruction]) = {
    this.instructions ++= instructions
    this
  }

  protected def self = this
}
