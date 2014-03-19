package sbtdocker

import sbt._
import Keys.{organization, name, streams}
import scala.util.Try

object Plugin extends sbt.Plugin {

  import DockerKeys._

  object DockerKeys {
    val docker = taskKey[Unit]("Creates a Docker image.")

    val dockerfile = taskKey[Dockerfile]("The Dockerfile that should be built.")
    val jarFile = taskKey[File]("Which JAR file to add to the image.")
    val stageDir = taskKey[File]("Staging directory use when building the image.")
    val imageName = taskKey[String]("Name of the built image.")
    val defaultImageName = taskKey[String]("Default name of the built image. Is used when imageName is not set.")
    val dockerPath = taskKey[String]("Path to the Docker binary.")
  }

  lazy val baseSettings = Seq(
    docker <<= (streams, dockerPath in docker, stageDir in docker, dockerfile in docker, jarFile in docker, imageName in docker) map {
      (streams, dockerPath, stageDir, dockerfile, jarPath, imageName) =>
        val log = streams.log
        log.debug("Generated Dockerfile:")
        log.debug(dockerfile.toString)

        DockerBuilder(dockerPath, dockerfile, imageName, stageDir, log)
    },
    dockerfile in docker <<= (dockerfile in docker),

    stageDir in docker <<= (stageDir in docker) or (Keys.target map (_ / "docker")),
    jarFile in docker := new File("temp"),
    imageName in docker <<= (imageName in docker) or (defaultImageName in docker),
    defaultImageName in docker <<= (organization, name) map {
      (organization, name) => s"$organization/$name"
    },
    dockerPath in docker := Try(System.getenv("DOCKER")).filter(s => s != null && s.nonEmpty).getOrElse("docker")
  )

  def basicSettings(image: Option[Either[String, JVM.Version]]) = baseSettings ++ Seq(
    dockerfile in docker <<= (stageDir in docker, jarFile in docker) map { (stageDir, jarFile) =>
      new Dockerfile {
        implicit val stageDirImplicit = stageDir

        val imageName = image match {
          case None => "totokaka/arch-java"
          case Some(Left(name)) => name
          case Some(Right(jvmVersion)) => ??? // TODO: return a trusted image - s"org/jre-$jvmVersion"
        }

        from(imageName)
        val targetJarFile = file("app") / jarFile.getName
        add(jarFile, targetJarFile)
        entryPoint("java", "-jar", targetJarFile.getPath)
      }
    }
  )

  def dockerSettings = baseSettings

  def dockerSettingsBasic = basicSettings(None)

  def dockerSettingsBasic(fromImage: String) = basicSettings(Some(Left(fromImage)))

  def dockerSettingsBasic(jvmVersion: JVM.Version) = basicSettings(Some(Right(jvmVersion)))

  object JVM extends Enumeration {
    val v6 = Value("1.6")
    val v7 = Value("1.7")
    type Version = Value
  }

}
