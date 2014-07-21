package sbtdocker.mutable

import sbtdocker.{CopyPath, DockerfileLike, Instruction}

/**
 * Mutable Dockerfile.
 *
 * @example {{{
 *  val jarFile: File
 *
 *  new Dockerfile {
 *    from("dockerfile/java")
 *    add(jarFile, "/srv/app.jar")
 *    workDir("/srv")
 *    cmd("java", "-jar", "app.jar")
 *  }
 *  }}}
 *
 * @param instructions Ordered sequence of Dockerfile instructions.
 * @param stagedFiles Files and directories that should be copied to the stage directory.
 */
case class Dockerfile(var instructions: Seq[Instruction] = Seq.empty,
                      var stagedFiles: Seq[CopyPath] = Seq.empty) extends DockerfileLike[Dockerfile] {

  def addInstruction(instruction: Instruction) = {
    instructions :+= instruction
    this
  }

  def stageFile(file: CopyPath) = {
    stagedFiles :+= file
    this
  }

  def stageFiles(files: TraversableOnce[CopyPath]) = {
    stagedFiles ++= files
    this
  }
}
