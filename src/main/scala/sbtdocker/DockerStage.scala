package sbtdocker

import sbt._
import sbtdocker.staging.{DefaultDockerfileProcessor, DockerfileProcessor, StagedDockerfile}

object DockerStage {
  def apply(stageDir: File, dockerfile: DockerfileLike, processor: DockerfileProcessor = DefaultDockerfileProcessor): Unit = {
    val staged = processor(dockerfile, stageDir)

    apply(stageDir, staged)
  }

  def apply(stageDir: File, staged: StagedDockerfile): Unit = {
    IO.delete(stageDir)

    IO.write(stageDir / "Dockerfile", staged.instructionsString)

    staged.stageFiles.foreach {
      case (source, destination) =>
        source.stage(destination)
    }
  }
}
