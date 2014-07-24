package sbtdocker

import sbt._
import scala.sys.process.{Process, ProcessLogger}

object DockerPush {
  /**
    * Push to a registry or docker hub
    *
    * @param imageName name of the resulting image
    * @param stageDir stage dir
    * @param log logger
    */
  def apply(dockerPath: String, imageName: ImageName, log: Logger) {
    log.info(s"Pushing docker image with name: '${imageName.name}'")

    val processLog = ProcessLogger({ line =>
                                     log.info(line)
                                   }, { line =>
                                     log.info(line)
                                   })

    val command = dockerPath :: "push" :: imageName.name :: Nil
    log.debug(s"Running command: '${command.mkString(" ")}'")

    val processOutput = Process(command).lines(processLog)
    processOutput.foreach { line =>
      log.info(line)
    }
  }
}
