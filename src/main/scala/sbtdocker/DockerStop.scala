package sbtdocker

import sbt.Logger

import sys.process.{Process, ProcessLogger}

/**
  *
  *
  * @author Matthew Pocock
  */
object DockerStop {
  def apply(dockerPath: String, stopOptions: StopOptions, log: Logger): ContainerId = {
    val processLogger = ProcessLogger({ line =>
          log.info(line)
        }, { line =>
          log.info(line)
        })

    if(stopOptions.containers.isEmpty)
      sys.error("Can not start an empty list of containers")
    val images = stopOptions.containers.to[List].map(_.id)

    log.info(s"Stopping containers: $images")

    val time = stopOptions.time map (t => "--time" :: t.toString :: Nil) getOrElse Nil

    val command = dockerPath :: "stop" :: time ::: images


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
