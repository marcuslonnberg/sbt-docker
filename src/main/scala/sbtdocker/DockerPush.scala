package sbtdocker

import sbt._

import scala.sys.process.{Process, ProcessLogger}

object DockerPush {

  val processLog: Logger => ProcessLogger = log => ProcessLogger({ line =>
    log.info(line)
  }, { line =>
    log.info(line)
  })

  /**
   * Push Docker images to a registry.
   *
   * @param dockerPath path to the docker binary
   * @param imageNames names of the images to push
   * @param log logger
   */
  def apply(dockerPath: String, imageNames: Seq[ImageName], log: Logger, dockerRegistryCredentials: Option[DockerRegistryCredentials] = None): Unit = {
    imageNames.foreach { imageName =>
      apply(dockerPath, imageName, log, dockerRegistryCredentials)
    }
  }

  /**
   * Push a Docker image to a registry.
   *
   * @param dockerPath path to the docker binary
   * @param imageName name of the image to push
   * @param log logger
   */
  def apply(dockerPath: String, imageName: ImageName, log: Logger, dockerRegistryCredentials: Option[DockerRegistryCredentials]): Unit = {
    log.info(s"Pushing docker image with name: '$imageName'")
    dockerRegistryCredentials.map(login).foreach(_(dockerPath, log))
    val command = dockerPath :: "push" :: imageName.toString :: Nil
    runCommand(command)("Failed to push", log)
    dockerRegistryCredentials.map(logout).foreach(_(dockerPath, log))
  }

  private def login(dockerRegistryCredentials: DockerRegistryCredentials): (String, Logger) => Unit = (dockerPath, log) => {
    log.info(s"Login to docker registry")
    val command = dockerPath :: "login" ::
      "-u" :: dockerRegistryCredentials.username ::
      "-p" :: dockerRegistryCredentials.password ::
      dockerRegistryCredentials.url :: Nil
    runCommand(command)("Failed to login", log)
  }

  private def logout(dockerRegistryCredentials: DockerRegistryCredentials): (String, Logger) => Unit = (dockerPath, log) => {
    log.info(s"Logout from docker registry")
    val command = dockerPath :: "logout" ::
      dockerRegistryCredentials.url :: Nil
    runCommand(command)("Failed to logout", log)
  }

  private def runCommand(command: Seq[String]): (String, Logger) => Unit = (errorMsg, log) => {
    log.debug(s"Running command: '${command.mkString(" ")}'")
    val process = Process(command)
    val exitValue = process ! processLog(log)
    if (exitValue != 0) sys.error(errorMsg)
  }
}
