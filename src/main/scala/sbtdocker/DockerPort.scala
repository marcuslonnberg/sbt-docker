package sbtdocker

import sbt.Logger

import scala.sys.process.{Process, ProcessLogger}

/**
  *
  *
  * @author Matthew Pocock
  */
object DockerPort {
  def apply(dockerPath: String, portOptions: PortOptions, log: Logger): PortMapping = {
    val processLogger = ProcessLogger({ line =>
          log.info(line)
        }, { line =>
          log.info(line)
    })

    val containerId = portOptions.container match {
      case Some(id) =>
        id
      case None =>
        sys.error("DockerCreate requires an imageId")
    }
    val container = containerId.id

    log.info(s"Scanning container for port mappings: $container")

    val command = dockerPath :: "port" :: container :: Nil

    val processOut = Process(command).lines(processLogger)

    processOut.foreach { line =>
      log.info(line)
    }

    PortMapping(
      processOut.collect {
        case PortMap.FromPortCommand(pm) => pm })
  }
}
