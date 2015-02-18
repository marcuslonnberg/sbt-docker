package sbtdocker

import sbt._
import staging.{DockerfileProcessor, StagedDockerfile}

import scala.sys.process.{Process, ProcessLogger}

object DockerBuilder {
  /**
   * Build a Dockerfile using a provided docker binary.
   *
   * @param dockerPath path to the docker binary
   * @param buildOptions options for the build command
   * @param imageNames names of the resulting image
   * @param dockerFile Dockerfile to build
   * @param stageDir stage dir
   * @param log logger
   */
  def apply(dockerPath: String, buildOptions: BuildOptions, imageNames: Seq[ImageName], dockerFile: DockerfileLike,
            stageDir: File, log: Logger): ImageId = {
  def apply(dockerfile: DockerfileLike, processor: DockerfileProcessor, imageName: ImageName, stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger) = {
    val staged = processor(dockerfile, stageDir)

    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    clean(stageDir)
    createDockerfile(staged, stageDir)
    prepareFiles(staged)
    build(imageNames, stageDir, dockerPath, buildOptions, log)
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

  def build(imageNames: Seq[ImageName], stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger): ImageId = {
    val processLog = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    val flags = List(
      buildOptions.noCache.map(value => s"--no-cache=$value"),
      buildOptions.rm.map(value => s"--rm=$value")).flatten

    val imageName = imageNames.headOption.map(i => List("-t", i.toString)).getOrElse(List())
    val tags = imageNames.tail
    val command = dockerPath :: "build" :: imageName ::: flags ::: "." :: Nil
    log.debug(s"Running command: '${command.mkString(" ")}' in '${stageDir.absString}'")

    val processOutput = Process(command, stageDir).lines(processLog)
    processOutput.foreach { line =>
      log.info(line)
    }

    val imageId = processOutput.collect {
      case SuccessfullyBuilt(id) => ImageId(id)
    }.lastOption

    imageId match {
      case Some(id) =>
        log.info(s"Successfully built Docker image: $imageName")
        log.info(s"Created image has id: ${id.id}")
        tags.foreach { tag =>
          log.info(s"Adding tag '$tag' to image")
          val command = dockerPath :: "tag" :: "-f" :: id.id :: tag.toString :: Nil
          val processOutput = Process(command).lines(processLog)
          processOutput.foreach { line =>
            log.info(line)
          }
        }
        id
      case None =>
        sys.error("Could not parse image id")
    }
  }
}
