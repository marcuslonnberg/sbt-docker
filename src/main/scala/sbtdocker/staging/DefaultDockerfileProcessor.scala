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
      case i: DockerfileInstruction =>
        context.addInstruction(i)

      case i: FileStagingDockerfileInstruction =>
        val count = context.stageFiles.size.toString
        val files = i.sources.map(source => source -> stageDir / count / source.filename)
        val sourcesInStaging = i.sources.map(source => s"$count/${source.filename}")

        val contextWithStagedFile =
          context.stageFiles(files.toSet)

        val dockerInstruction = i.dockerInstruction(sourcesInStaging)
        contextWithStagedFile.addInstruction(dockerInstruction)

      case i: FileStagingInstruction =>
        val files = i.sources.map(source => source -> stageDir / expandPath(i.destination, source).getPath)
        context.stageFiles(files.toSet)
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
