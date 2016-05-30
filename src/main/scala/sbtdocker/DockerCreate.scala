package sbtdocker

import sbt.Logger

import sys.process.{Process, ProcessLogger}

/**
  *
  *
  * @author Matthew Pocock
  */
object DockerCreate {
  def apply(dockerPath: String, createOptions: CreateOptions, log: Logger): ContainerId = {
    val processLogger = ProcessLogger({ line =>
          log.info(line)
        }, { line =>
          log.info(line)
        })

    log.info(s"Creating container from image ${createOptions.imageId}")

    val e = createOptions.exposes.to[List] flatMap(e => "--expose" :: e.toString :: Nil)
    val p = createOptions.ports.to[List] flatMap(p => "-p" :: p.toString :: Nil)

    val imageId = createOptions.imageId match {
      case Some(id) =>
        id
      case None =>
        sys.error("DockerCreate requires an imageId")
    }

    val command = dockerPath :: "create" :: p ::: imageId :: Nil

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
