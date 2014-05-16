package sbtdocker

import sbt._
import Keys.{organization, name, streams}
import scala.util.Try
import sbtdocker.Dockerfile.StageDir

object Plugin extends sbt.Plugin {

  import DockerKeys._

  object DockerKeys {
    val docker = taskKey[ImageId]("Creates a Docker image.")

    val dockerfile = taskKey[Dockerfile]("The Dockerfile that should be built.")
    val jarFile = taskKey[File]("JAR file to add to the image.")
    val stageDir = taskKey[StageDir]("Staging directory used when building the image.")
    val imageName = taskKey[ImageName]("Name of the built image.")
    val defaultImageName = taskKey[ImageName]("Default name of the built image. Is used when imageName is not set.")
    val dockerPath = taskKey[String]("Path to the Docker binary.")
    val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
  }

  lazy val baseSettings = Seq(
    docker <<= (streams, dockerPath in docker, buildOptions in docker, stageDir in docker, dockerfile in docker, jarFile in docker, imageName in docker) map {
      (streams, dockerPath, buildOptions, stageDir, dockerfile, jarPath, imageName) =>
        val log = streams.log
        log.debug("Generated Dockerfile:")
        log.debug(dockerfile.toInstructionsString)

        DockerBuilder(dockerPath, buildOptions, imageName, dockerfile, stageDir, log)
    },
    dockerfile in docker <<= (dockerfile in docker),

    stageDir in docker <<= (stageDir in docker) or (Keys.target map (target => StageDir(target / "docker"))),
    jarFile in docker := new File("temp"),
    imageName in docker <<= (imageName in docker) or (defaultImageName in docker),
    defaultImageName in docker <<= (organization, name) map {
      case ("", name) =>
        ImageName(name)
      case (organization, name) =>
        ImageName(namespace = Some(organization), repository = name)
    },
    dockerPath in docker := Try(System.getenv("DOCKER")).filter(s => s != null && s.nonEmpty).getOrElse("docker"),
    buildOptions in docker := BuildOptions()
  )

  def singleJarSettings(fromImage: String) = baseSettings ++ Seq(
    dockerfile in docker <<= (stageDir in docker, jarFile in docker) map { (stageDir, jarFile) =>
      val targetJarFile = file("/app") / jarFile.getName

      new Dockerfile {
        from(fromImage)
        add(jarFile, targetJarFile.toPath)
        entryPoint("java", "-jar", targetJarFile.getPath)
      }
    }
  )

  def dockerSettings = baseSettings

  /**
   * Defines a simple Dockerfile that only adds a single JAR and starts it with 'java -jar application.jar'.
   * The source JAR file is specified with the key 'jarFile'.
   * Uses 'dockerfile/java' as base image.
   */
  def dockerSettingsSingleJar = singleJarSettings(fromImage = "dockerfile/java")

  def dockerSettingsSingleJar(fromImage: String) = singleJarSettings(fromImage)

}
