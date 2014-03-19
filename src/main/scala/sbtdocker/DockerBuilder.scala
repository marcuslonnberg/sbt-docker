package sbtdocker

import sbt._
import scala.sys.process.{Process, ProcessLogger}
import scala.sys.error
import sbtdocker.Dockerfile.CopyFile

object DockerBuilder {
  /**
   * Build a Dockerfile using a provided docker binary.
   *
   * @param dockerPath path to the docker binary
   * @param dockerFile Dockerfile to build
   * @param imageName name of the resulting image
   * @param stageDir stage dir
   * @param log logger
   */
  def apply(dockerPath: String, dockerFile: Dockerfile, imageName: String, stageDir: File, log: Logger) = {
    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    IO.delete(stageDir)

    IO.write(stageDir / "Dockerfile", dockerFile.toString)
    copyFiles(dockerFile.pathsToCopy, stageDir, log)

    log.info(s"Starting to build Dockerfile")
    val processLog = ProcessLogger({ line =>
      log.info(line)
    })
    val command = Seq(dockerPath, "build", "-t", imageName, ".")
    log.info(s"Will run command: $command")
    Process(command, stageDir) ! processLog match {
      case 0 => log.info(s"Successfully built docker image: $imageName")
      case n => error(s"Error when building Dockerfile, exit code: $n")
    }
  }

  def copyFiles(pathsToCopy: Seq[CopyFile], stageDir: File, log: Logger) = {
    for (CopyFile(source, targetRelative) <- pathsToCopy) {
      val target = stageDir / targetRelative.getPath
      log.debug(s"Copying '${source.getPath}' to '${target.getPath}'")

      if (target.exists()) {
        error(s"""Path "${target.getPath}" already exists in stage directory""")
      }

      if (source.isFile)
        IO.copyFile(source, target, preserveLastModified = true)
      else if (source.isDirectory)
        IO.copyDirectory(source, target, overwrite = false, preserveLastModified = true)
    }
  }
}
