package sbtdocker

import sbt.Logger

import sys.process.{Process, ProcessLogger}

/**
  *
  *
  * @author Matthew Pocock
  */
object DockerStart {
  def apply(dockerPath: String, startOptions: StartOptions, log: Logger): ContainerId = {
    val processLogger = ProcessLogger({ line =>
          log.info(line)
        }, { line =>
          log.info(line)
        })

    if(startOptions.containers.isEmpty)
      sys.error("Can not start an empty list of containers")
    val images = startOptions.containers.to[List].map(_.id)

    log.info(s"Starting containers: $images")

    val command = dockerPath :: "start" :: images


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
