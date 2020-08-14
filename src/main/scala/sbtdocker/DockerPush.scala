package sbtdocker

import sbt._

import scala.sys.process.{Process, ProcessLogger}

object DockerPush {

  /**
    * Push Docker images to a registry.
    *
    * @param dockerPath path to the docker binary
    * @param imageNames names of the images to push
    * @param log logger
    */
  def apply(dockerPath: String, imageNames: Seq[ImageName], log: Logger): Map[ImageName, ImageDigest] = {
    imageNames.map { imageName =>
      apply(dockerPath, imageName, log)
    }.toMap
  }

  /**
    * Push a Docker image to a registry.
    *
    * @param dockerPath path to the docker binary
    * @param imageName name of the image to push
    * @param log logger
    */
  def apply(dockerPath: String, imageName: ImageName, log: Logger): (ImageName, ImageDigest) = {
    log.info(s"Pushing docker image with name: '$imageName'")

    var lines = Seq.empty[String]
    val processLog = ProcessLogger(
      { line =>
        log.info(line)
        lines :+= line
      },
      { line =>
        log.info(line)
        lines :+= line
      }
    )

    val command = dockerPath :: "push" :: imageName.toString :: Nil
    log.debug(s"Running command: '${command.mkString(" ")}'")

    val process = Process(command)
    val exitValue = process ! processLog
    if (exitValue != 0) sys.error("Failed to push")

    val PushedImageDigestSha256 = ".* digest: sha256:([0-9a-f]+) .*".r

    val imageId = lines.collect {
      case PushedImageDigestSha256(digest) => ImageDigest("sha256", digest)
    }.lastOption

    imageId match {
      case Some(id) =>
        imageName -> id
      case None =>
        throw new DockerPushException("Could not parse Docker image id")
    }
  }
}

class DockerPushException(message: String) extends RuntimeException(message)
