package sbtdocker

import sbt._
import staging.{DockerfileProcessor, StagedDockerfile}

import scala.sys.process.{Process, ProcessLogger}

object DockerBuilder {
  /**
   * Build a Dockerfile using a provided docker binary.
   *
   * @param dockerfile Dockerfile to build
   * @param processor processor to create a staging directory for the Dockerfile
   * @param imageNames names of the resulting image
   * @param stageDir stage dir
   * @param dockerPath path to the docker binary
   * @param buildOptions options for the build command
   * @param log logger
   */
  def apply(dockerfile: DockerfileLike, processor: DockerfileProcessor, imageNames: Seq[ImageName],
            stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger) = {
    val staged = processor(dockerfile, stageDir)

    log.debug("Building Dockerfile:\n" + staged.instructionsString)

    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    clean(stageDir)
    createDockerfile(staged, stageDir)
    prepareFiles(staged)
    buildAndTag(imageNames, stageDir, dockerPath, buildOptions, log)
  }

  def clean(stageDir: File) = {
    IO.delete(stageDir)
  }

  def createDockerfile(staged: StagedDockerfile, stageDir: File) = {
    IO.write(stageDir / "Dockerfile", staged.instructionsString)
  }

  def prepareFiles(staged: StagedDockerfile) = {
    staged.stageFiles.foreach {
      case (source, destination) =>
        source.stage(destination)
    }
  }

  private val SuccessfullyBuilt = "^Successfully built ([0-9a-f]+)$".r

  def buildAndTag(imageNames: Seq[ImageName], stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger): ImageId = {
    val processLogger = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    val imageId = build(stageDir, dockerPath, buildOptions, log, processLogger)

    imageNames.foreach { name =>
      tag(imageId, name, dockerPath, processLogger, log)
    }
    
    imageId
  }

  def build(stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger, processLogger: ProcessLogger): ImageId = {
    val flags = List(
      buildOptions.noCache.map(value => s"--no-cache=$value"),
      buildOptions.rm.map(value => s"--rm=$value")).flatten

    val command = dockerPath :: "build" :: flags ::: "." :: Nil
    log.debug(s"Running command: '${command.mkString(" ")}' in '${stageDir.absString}'")

    val processOutput = Process(command, stageDir).lines(processLogger)
    processOutput.foreach { line =>
      log.info(line)
    }

    val imageId = processOutput.collect {
      case SuccessfullyBuilt(id) => ImageId(id)
    }.lastOption

    imageId match {
      case Some(id) =>
        id
      case None =>
        sys.error("Could not parse image id")
    }
  }

  def tag(id: ImageId, name: ImageName, dockerPath: String, processLogger: ProcessLogger, log: Logger): Unit = {
    log.info(s"Tagging image $id with name: $name")
    val command = dockerPath :: "tag" :: "-f" :: id.id :: name.toString :: Nil
    val processOutput = Process(command).lines(processLogger)
    processOutput.foreach { line =>
      log.info(line)
    }
  }
}
