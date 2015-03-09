package sbtdocker.staging

import sbt._
import sbtdocker._

/**
 * DockerfileProcessor that stages all files in the docker staging directory as `/{index}/file`.
 * Where index is a counter that increases for each file that is staged.
 */
object DefaultDockerfileProcessor extends DockerfileProcessor {
  def apply(dockerfile: DockerfileLike, stageDir: File) = {
    dockerfile.instructions
      .foldLeft(StagedDockerfile.empty)(handleInstruction(stageDir))
  }

  private[sbtdocker] def handleInstruction(stageDir: File)(context: StagedDockerfile, instruction: Instruction): StagedDockerfile = {
    instruction match {
      case instruction: DockerfileInstruction =>
        context.addInstruction(instruction)

      case instruction: FileStagingDockerfileInstruction =>
        val count = context.stageFiles.size

        def fileStagePath(source: SourceFile, index: Int): String = (count + index) + "/" + source.filename

        val contextWithStagedFiles = {
          val files = instruction.sources.zipWithIndex.map {
            case (source, index) =>
              source -> stageDir / fileStagePath(source, index)
          }
          context.stageFiles(files.toSet)
        }

        val dockerInstruction = {
          val sourcesInStaging = instruction.sources.zipWithIndex.map {
            case (source, index) => fileStagePath(source, index)
          }
          instruction.dockerInstruction(sourcesInStaging)
        }

        contextWithStagedFiles.addInstruction(dockerInstruction)

      case instruction: FileStagingInstruction =>
        val files = instruction.sources.map { source =>
          source -> stageDir / expandPath(instruction.destination, source).getPath
        }
        context.stageFiles(files.toSet)
    }
  }

  private[sbtdocker] def expandPath(destination: String, source: SourceFile): File = {
    if (destination.endsWith("/")) {
      file(destination) / source.filename
    } else {
      file(destination)
    }
  }
}
