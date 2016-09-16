package sbtdocker

import sbt._

import scala.sys.process.{Process, ProcessLogger}
import scala.util.{Failure, Success, Try}

object DockerCopy {
  /**
   * Copy Docker images to a docker machine.
   *
   * @param dockerMachinePath path to the docker machine binary
   * @param imageNames names of the images to push
   * @param log logger
   */
  def apply(dockerPath: String, dockerMachinePath: String, dockerMachineName: String, imageNames: Seq[ImageName], log: Logger): Unit = {
    imageNames.foreach { imageName =>
      apply(dockerPath, dockerMachinePath, dockerMachineName, imageName, log)
    }
  }

  /**
   * Copy a Docker image to a docker machine.
   *
   * @param dockerMachinePath path to the docker machine binary
   * @param imageName name of the image to push
   * @param log logger
   */
  def apply(dockerPath: String, dockerMachinePath: String, dockerMachineName: String, imageName: ImageName, log: Logger): Unit = {
    log.info(s"Copying docker image with name: '$imageName' to machine '$dockerMachineName'")

    val processLog = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    def configCommand = dockerMachinePath :: "config" :: dockerMachineName :: Nil

    log.debug(s"Running command: '${configCommand.mkString(" ")}'")

    Try(Process(configCommand) lines_! processLog) match {
      case Success(dockerMachineConfig) =>
        val saveCommand = dockerPath :: "save" ::  imageName.toString :: Nil
        val loadCommand = (dockerPath :: dockerMachineConfig.toList) :+ "load"
        log.debug(s"Running command: '${saveCommand.mkString(" ")} | ${loadCommand.mkString(" ")}'")
        val exitValue = Process(saveCommand) #| Process(loadCommand) ! processLog
        if (exitValue != 0) sys.error("Failed to copy image")
      case Failure(_) =>
        sys.error("Failed to get docker-machine config")
    }

  }
}