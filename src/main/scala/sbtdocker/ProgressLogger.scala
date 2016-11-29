package sbtdocker

import com.spotify.docker.client.ProgressHandler
import com.spotify.docker.client.messages.ProgressMessage
import sbt.Logger

class ProgressLogger(logger: Logger) extends ProgressHandler {
  override def progress(message: ProgressMessage): Unit = {
    message.error() match {
      case null | "" =>
        Option(message.stream()).filter(_.nonEmpty).foreach { stream =>
          logger.info(stream)
        }
      case error =>
        logger.error(error)
    }

  }
}
