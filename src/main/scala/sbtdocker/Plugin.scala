package sbtdocker

import sbt._
import Keys.{organization, name, streams}

object Plugin extends sbt.Plugin {

  import DockerKeys._

  object DockerKeys {
    val docker = TaskKey[Unit]("docker", "Creates a docker container ")

    val dockerfile = TaskKey[Dockerfile]("docker-dockerfile")
    val jarFile = TaskKey[File]("docker-jar-file")
    val stageDir = TaskKey[File]("docker-target")
    val imageName = TaskKey[String]("docker-image-name")
    val defaultImageName = TaskKey[String]("docker-default-image-name")
  }

  lazy val baseSettings = Seq(
    docker <<= (streams, stageDir in docker, dockerfile in docker, jarFile in docker, imageName in docker) map {
      (streams, stageDir, dockerfile, jarPath, imageName) =>
        val log = streams.log
        log.debug("Generated Dockerfile:")
        log.debug(dockerfile.toString)

        DockerBuilder(dockerfile, imageName, stageDir, log)
    },
    dockerfile in docker <<= (dockerfile in docker),

    stageDir in docker <<= (stageDir in docker) or (Keys.target map (_ / "docker")),
    jarFile in docker := new File("temp"),
    imageName in docker <<= (imageName in docker) or (defaultImageName in docker),
    defaultImageName in docker <<= (organization, name) map {
      (organization, name) => s"$organization/$name"
    }
  )

  def basicSettings(image: Option[Either[String, JVM.Version]]) = baseSettings ++ Seq(
    dockerfile in docker <<= (stageDir in docker, jarFile in docker) map {
      (stageDir, jarFile) => new Dockerfile {
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
