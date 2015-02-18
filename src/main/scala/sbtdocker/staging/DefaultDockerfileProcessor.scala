package sbtdocker.staging

import sbt._
import sbtdocker._

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
