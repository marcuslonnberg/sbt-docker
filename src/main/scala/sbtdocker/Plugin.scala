package sbtdocker

import sbt._
import Keys.{organization, name, streams}
import scala.util.Try
import sbtdocker.Dockerfile.StageDir

object Plugin extends sbt.Plugin {

  import DockerKeys._

  val NamespaceNameDisallowedChars = "[^a-z0-9_]".r
  val RepositoryNameDisallowedChars = "[^a-z0-9-_\\.]".r

  object DockerKeys {
    val docker = taskKey[Unit]("Creates a Docker image.")

    val dockerfile = taskKey[Dockerfile]("The Dockerfile that should be built.")
    val jarFile = taskKey[File]("JAR file to add to the image.")
    val stageDir = taskKey[StageDir]("Staging directory used when building the image.")
    val imageName = taskKey[String]("Name of the built image.")
    val defaultImageName = taskKey[String]("Default name of the built image. Is used when imageName is not set.")
    val dockerPath = taskKey[String]("Path to the Docker binary.")
  }

  lazy val baseSettings = Seq(
    docker <<= (streams, dockerPath in docker, stageDir in docker, dockerfile in docker, jarFile in docker, imageName in docker) map {
      (streams, dockerPath, stageDir, dockerfile, jarPath, imageName) =>
        val log = streams.log
        log.debug("Generated Dockerfile:")
        log.debug(dockerfile.toInstructionsString)

        DockerBuilder(dockerPath, dockerfile, imageName, stageDir, log)
    },
    dockerfile in docker <<= (dockerfile in docker),

    stageDir in docker <<= (stageDir in docker) or (Keys.target map (target => StageDir(target / "docker"))),
    jarFile in docker := new File("temp"),
    imageName in docker <<= (imageName in docker) or (defaultImageName in docker),
    defaultImageName in docker <<= (organization, name) map {
      case ("", name) => name
      case (organization, name) =>
        val namespaceName = NamespaceNameDisallowedChars.replaceAllIn(organization, "")
        val repositoryName = RepositoryNameDisallowedChars.replaceAllIn(name, "")
        s"$namespaceName/$repositoryName"
    },
    dockerPath in docker := Try(System.getenv("DOCKER")).filter(s => s != null && s.nonEmpty).getOrElse("docker")
  )

  def basicSettings(image: Option[String]) = baseSettings ++ Seq(
    dockerfile in docker <<= (stageDir in docker, jarFile in docker) map { (stageDir, jarFile) =>
      new Dockerfile {
        implicit val stageDirImplicit = stageDir

        val imageName = image.getOrElse("totokaka/arch-java")

        from(imageName)
        val targetJarFile = file("app") / jarFile.getName
        add(jarFile, targetJarFile)
        entryPoint("java", "-jar", targetJarFile.getPath)
      }
    }
  )

  def dockerSettings = baseSettings

  def dockerSettingsBasic = basicSettings(None)

  def dockerSettingsBasic(fromImage: String) = basicSettings(Some(fromImage))

}
