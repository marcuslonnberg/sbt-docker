package sbtdocker

import com.spotify.docker.client.DefaultDockerClient
import sbt.Keys._
import sbt.{Keys, _}
import sbtdocker.DockerKeys._

object DockerSettings {
  def baseDockerSettings = Seq(
    dockerClient in docker := {
      DefaultDockerClient.fromEnv().build()
    },
    buildOptions in docker := BuildOptions(),
    dockerBuildOptions in docker := (buildOptions in docker).value,
    target in docker := target.value / "docker",
    docker := {
      val stageDir = target.in(docker).value
      DockerStage(stageDir, dockerfile.in(docker).value)

      val logger = Keys.streams.value.log
      logger.info(s"Building Docker image")
      val client = (dockerClient in docker).value
      val progressHandler = new ProgressLogger(logger)
      val imageId = client.build(stageDir.toPath, progressHandler, DockerClientHelpers.buildParams(dockerBuildOptions.in(docker).value): _*)

      val imageNames = (dockerImageNames in docker).value
      imageNames.foreach { imageName =>
        logger.info(s"Tagging Docker image $imageId with $imageName")
        client.tag(imageId, imageName.toString, true)
      }
    },
    dockerfile in docker := {
      sys.error {
        """A Dockerfile is not defined. Please define one with `dockerfile in docker`
          |
          |Example:
          |dockerfile in docker := new Dockerfile {
          | from("ubuntu")
          | ...
          |}
        """.stripMargin
      }
    },
    dockerPush in docker := {
      val client = (dockerClient in docker).value
      val imageNames = (dockerImageNames in dockerPush).value

      val logger = Keys.streams.value.log
      val progressHandler = new ProgressLogger(logger)
      imageNames.foreach { imageName =>
        logger.info(s"Pushing Docker image $imageName")
        client.push(imageName.toString, progressHandler)
      }
    },
    dockerPush := (dockerPush in docker).value,
    dockerBuildAndPush in docker := {
      docker.value
      (dockerPush in docker).value
    },
    dockerBuildAndPush := (dockerBuildAndPush in docker).value,
    publish in docker := (dockerBuildAndPush in docker).value,
    publishLocal in docker := (docker in docker).value,
    imageNames in docker := {
      val organisation = Option(Keys.organization.value).filter(_.nonEmpty)
      val name = Keys.normalizedName.value
      Seq(ImageName(namespace = organisation, repository = name))
    },
    dockerImageNames in docker := imageNames.in(docker).value,
    dockerImageNames in dockerPush := dockerImageNames.in(docker).value
  )
}
