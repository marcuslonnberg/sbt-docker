package sbtdocker

import sbt.Keys.target
import sbt._
import sbtdocker.DockerKeys._

object DockerSettings {
  lazy val baseDockerSettings = Seq(
    docker := {
      val log = Keys.streams.value.log
      val dockerCmd = (DockerKeys.dockerCmd in docker).value
      val buildOptions = (DockerKeys.buildOptions in docker).value
      val stageDir = (target in docker).value
      val dockerfile = (DockerKeys.dockerfile in docker).value
      val imageName = (DockerKeys.imageName in docker).value

      log.debug("Dockerfile:")
      log.debug(dockerfile.mkString)

      DockerBuilder(dockerCmd, buildOptions, imageName, dockerfile, stageDir, log)
    },
    dockerPush := {
      val log = Keys.streams.value.log
      val dockerCmd = (DockerKeys.dockerCmd in docker).value
      val imageName = (DockerKeys.imageName in docker).value

      DockerPush(dockerCmd, imageName, log)
    },
    dockerBuildAndPush := {
      val imageId = docker.value
      dockerPush.value
      imageId
    },
    dockerfile in docker := {
      sys.error(
        """A Dockerfile is not defined. Please define it with `dockerfile in docker`
          |
          |Example:
          |dockerfile in docker := new Dockerfile {
          | from("ubuntu")
          | ...
          |}
        """.stripMargin)
    },
    target in docker := target.value / "docker",
    imageName in docker := {
      val organisation = Option(Keys.organization.value).filter(_.nonEmpty)
      val name = Keys.normalizedName.value
      ImageName(namespace = organisation, repository = name)
    },
    dockerCmd in docker := sys.env.get("DOCKER").filter(_.nonEmpty).getOrElse("docker"),
    buildOptions in docker := BuildOptions()
  )

  def packageDockerSettings(fromImage: String, exposePorts: Seq[Int]) = Seq(
    docker <<= docker.dependsOn(Keys.`package`.in(Compile, Keys.packageBin)),
    Keys.mainClass in docker <<= Keys.mainClass in docker or Keys.mainClass.in(Compile, Keys.packageBin),
    dockerfile in docker <<= (Keys.managedClasspath in Compile, Keys.artifactPath.in(Compile, Keys.packageBin), Keys.mainClass in docker) map {
      case (_, _, None) =>
        sys.error("No main class found or multiple main classes exists. " +
          "One can be set with 'mainClass in docker := Some(\"package.MainClass\")'.")
      case (classpath, artifact, Some(mainClass)) =>
        val appPath = "/app"
        val libsPath = s"$appPath/libs"
        val artifactPath = s"$appPath/${artifact.name}"

        val dockerfile = Dockerfile()
        dockerfile.from(fromImage)

        if (exposePorts.nonEmpty) {
          dockerfile.expose(exposePorts: _*)
        }

        val libPaths = classpath.files.map { libFile =>
          val toPath = file(libsPath) / libFile.name
          dockerfile.copyToStageDir(libFile, toPath)
          toPath
        }
        val classpathString = s"${libPaths.mkString(":")}:$artifactPath"

        dockerfile.add(libsPath, libsPath)
        dockerfile.add(artifact, artifactPath)
        dockerfile.entryPoint("java", "-cp", classpathString, mainClass)

        dockerfile
    }
  )
}
