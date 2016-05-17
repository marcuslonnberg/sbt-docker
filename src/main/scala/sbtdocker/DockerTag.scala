package sbtdocker

import sbt.Logger

import sys.process.{Process, ProcessLogger}

object DockerTag {
  def apply(id: ImageId, name: ImageName, dockerPath: String, log: Logger): Unit = {
    val processLogger = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    log.info(s"Tagging image $id with name: $name")

    val version = DockerVersion(dockerPath, log)
    val flags = if(version.major <= 1 && version.minor < 10) "-f" :: Nil else Nil
    val command = dockerPath :: "tag" :: flags ::: id.id :: name.toString :: Nil

    val processOutput = Process(command).lines(processLogger)
    processOutput.foreach { line =>
      log.info(line)
    }
  }
}
