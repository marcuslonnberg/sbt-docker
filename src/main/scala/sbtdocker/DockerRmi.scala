package sbtdocker

import sbt._

import scala.sys.process.{Process, ProcessLogger}

object DockerRmi {

  /**
    * Delete Docker images.
    *
    * @param dockerPath path to the docker binary
    * @param imageNames names of the images to delete
    * @param log logger
    */
  def apply(dockerPath: String, imageNames: Seq[ImageName], log: Logger): Map[ImageName, Seq[ImageDigest]] = {
    imageNames.map { imageName =>
      apply(dockerPath, imageName, log)
    }.toMap
  }

  /**
    * Delete a Docker image.
    *
    * @param dockerPath path to the docker binary
    * @param imageName name of the image to delete
    * @param log logger
    */
  def apply(dockerPath: String, imageName: ImageName, log: Logger): (ImageName, Seq[ImageDigest]) = {
    log.info(s"Deleting docker image with name: '$imageName'")

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

    val command = dockerPath :: "rmi" :: "-f" :: imageName.toString :: Nil
    log.debug(s"Running command: '${command.mkString(" ")}'")

    val process = Process(command)
    val exitCode = process ! processLog
    if (exitCode != 0) throw new DockerRmiException(s"Failed to run 'docker rmi' on image $imageName. Exit code $exitCode")

    val DeletedImageDigestSha256 = ".*Deleted: ([^:]*):([0-9a-f]+).*".r

    val deletedImages = lines.collect {
      case DeletedImageDigestSha256(algo, digest) => ImageDigest(algo, digest)
    }

    (imageName, deletedImages)
  }
}

class DockerRmiException(message: String) extends RuntimeException(message)
