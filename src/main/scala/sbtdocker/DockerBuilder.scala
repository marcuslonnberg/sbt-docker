package sbtdocker

import sbt._
import scala.sys.process.{Process, ProcessLogger}

object DockerBuilder {
  /**
   * Build a Dockerfile using a provided docker binary.
   *
   * @param dockerPath path to the docker binary
   * @param buildOptions options for the build command
   * @param imageName name of the resulting image
   * @param dockerFile Dockerfile to build
   * @param stageDir stage dir
   * @param log logger
   */
  def apply(dockerPath: String, buildOptions: BuildOptions, imageName: ImageName, dockerFile: DockerfileLike,
            stageDir: File, log: Logger): ImageId = {
    log.info(s"Creating docker image with name: '$imageName'")

    prepareFiles(dockerFile, stageDir, log)

    buildImage(dockerPath, buildOptions, imageName, stageDir, log)
  }

  def prepareFiles(dockerFile: DockerfileLike, stageDir: File, log: Logger) = {
    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    IO.delete(stageDir)

    IO.write(stageDir / "Dockerfile", dockerFile.mkString)
    copyFiles(dockerFile.stagedFiles, stageDir, log)
    copyFiles(dockerFile.stagedArchives.map(_.outputFile), stageDir, log)
  }

  def copyFiles(files: Seq[StageFile], stageDir: File, log: Logger) = {
    for (StageFile(source, targetRelative) <- files.distinct) {
      copyFile(source, targetRelative, stageDir, log)
    }
  }

  def copyFile(source: File, targetRelative: File, stageDir: File, log: Logger) {
    val target = stageDir / targetRelative.getPath
    log.debug(s"Copying '${source.getPath}' to '${target.getPath}'")

    if (source == target) {
      log.debug(s"Source file is already in the stage directory '${source.getPath}'")
    } else {
      if (target.exists()) {
        log.warn(s"""Path "${target.getPath}" already exists in the stage directory""")
      }

      if (source.isFile) {
        IO.copyFile(source, target, preserveLastModified = true)
      } else if (source.isDirectory) {
        IO.copyDirectory(source, target, overwrite = false, preserveLastModified = true)
      } else {
        log.error(s"Unknown type of path '${source.getPath}'")
      }
    }
  }

  private val SuccessfullyBuilt = "^Successfully built ([0-9a-f]+)$".r

  def buildImage(dockerPath: String, buildOptions: BuildOptions, imageName: ImageName, stageDir: File, log: Logger): ImageId = {
    val processLog = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    val flags = List(
      buildOptions.noCache.map(value => s"--no-cache=$value"),
      buildOptions.rm.map(value => s"--rm=$value")).flatten

    val command = dockerPath :: "build" :: "-t" :: imageName.toString :: flags ::: "." :: Nil
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
        id
      case None =>
        sys.error("Could not parse image id")
    }
  }
}
