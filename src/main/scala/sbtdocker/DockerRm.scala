package sbtdocker

import sbt.Logger

import scala.sys.process.{Process, ProcessLogger}

/**
  *
  *
  * @author Matthew Pocock
  */
object DockerRm {
  def apply(dockerPath: String, rmOptions: RmOptions, log: Logger): ContainerId = {
    val processLogger = ProcessLogger({ line =>
          log.info(line)
        }, { line =>
          log.info(line)
        })

    if(rmOptions.containers.isEmpty)
      sys.error("Can not start an empty list of containers")
    val images = rmOptions.containers.to[List].map(_.id)

    log.info(s"Removing containers: $images")

    val command = dockerPath :: "rm" :: images


    val processOut = Process(command).lines(processLogger)

    processOut.foreach { line =>
      log.info(line)
    }

    (processOut.collect { case ContainerId(cid) => cid }).lastOption match {
      case Some(cid) =>
        cid
      case None =>
        sys.error("Could not parse container id")
    }
  }
}
