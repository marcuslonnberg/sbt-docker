package sbtdocker

import sbt._

object StagedDockerfile {
  def empty = StagedDockerfile(Seq.empty, Set.empty)
}

case class StagedDockerfile(instructions: Seq[DockerInstruction], stageFiles: Set[(SourceFile, File)]) {
  def addInstruction(instruction: DockerInstruction) = copy(instructions = instructions :+ instruction)

  def stageFile(source: SourceFile, destination: File) = copy(stageFiles = stageFiles + (source -> destination))

  def stageFiles(files: Set[(SourceFile, File)]) = copy(stageFiles = stageFiles ++ files)

  def instructionsString = instructions.mkString("\n")
}

trait SourceFile {
  def filename: String

  def stage(stageDir: File): Unit
}

case class CopyFile(file: File) extends SourceFile {
  def filename = file.getName

  def stage(destination: File) = {
    if (file.isDirectory) {
      IO.copyDirectory(file, destination)
    } else {
      IO.copyFile(file, destination)
    }
  }
}


object CopyTree {
  def exact(base: File) = CopyTree(base, keepSymlinks = true, keepFileFlags = true)
}

case class CopyTree(base: File, keepSymlinks: Boolean = false, keepFileFlags: Boolean = false) extends SourceFile {
  def filename = ???

  def stage(stageDir: File) = ???
}

trait DockerfileProcessor {
  def apply(dockerfile: DockerfileLike, stageDir: File): StagedDockerfile
}

object DefaultDockerfileProcessor extends DockerfileProcessor {
  def apply(dockerfile: DockerfileLike, stageDir: File) = {
    dockerfile.instructions
      .foldLeft(StagedDockerfile.empty)(handleInstruction(stageDir))
  }

  private[sbtdocker] def handleInstruction(stageDir: File)(context: StagedDockerfile, instruction: Instruction): StagedDockerfile = {
    instruction match {
      case i: DockerInstruction =>
        context.addInstruction(i)

      case i: StagedFileInstruction =>
        val files = i.sources.map(source => source -> stageDir / expandPath(i.destination, source).getPath)
        context.stageFiles(files.toSet)

      case i: FileInstruction =>
        // TODO: handle stagefile correctly
        val count = context.stageFiles.size.toString
        // TODO: handle file patterns  - http://golang.org/pkg/path/filepath/#Match
        val files = i.sources.map(source => source -> stageDir / count / source.filename)
        val sourcesInStaging = i.sources.map(source => s"$count/${source.filename}")

        val contextWithStagedFile =
          context.stageFiles(files.toSet)

        i.dockerInstruction(sourcesInStaging, i.destination) match {
          case Some(outInstruction) =>
            contextWithStagedFile.addInstruction(outInstruction)
          case None =>
            contextWithStagedFile
        }
    }
  }

  def expandPath(destination: String, source: SourceFile): File = {
    if (destination.endsWith("/")) {
      file(destination) / source.filename
    } else {
      file(destination)
    }
  }
}
