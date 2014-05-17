package sbtdocker

import sbt._
import Keys._
import sbtdocker.Dockerfile.StageDir

object Plugin extends sbt.Plugin {

  import DockerKeys._

  object DockerKeys {
    val docker = taskKey[ImageId]("Creates a Docker image.")

    val dockerfile = taskKey[Dockerfile]("Definition of the Dockerfile that should be built.")
    val stageDir = taskKey[StageDir]("Staging directory used when building the image.")
    val imageName = taskKey[ImageName]("Name of the built image.")
    val dockerPath = settingKey[String]("Path to the Docker binary.")
    val buildOptions = settingKey[BuildOptions]("Options for the Docker build command.")
  }

  lazy val baseDockerSettings = Seq(
    docker <<= (streams, dockerPath in docker, buildOptions in docker, stageDir in docker, dockerfile in docker, imageName in docker) map {
      (streams, dockerPath, buildOptions, stageDir, dockerfile, imageName) =>
        val log = streams.log
        log.debug("Generated Dockerfile:")
        log.debug(dockerfile.toInstructionsString)

        DockerBuilder(dockerPath, buildOptions, imageName, dockerfile, stageDir, log)
    },
    stageDir in docker <<= target map (target => StageDir(target / "docker")),
    imageName in docker <<= (organization, name) map {
      case ("", name) =>
        ImageName(name)
      case (organization, name) =>
        ImageName(namespace = Some(organization), repository = name)
    },
    dockerPath in docker := sys.env.get("DOCKER").filter(_.nonEmpty).getOrElse("docker"),
    buildOptions in docker := BuildOptions()
  )

  def packageDockerSettings(fromImage: String, exposePorts: Seq[Int]) = Seq(
    docker <<= docker.dependsOn(Keys.`package`.in(Compile, packageBin)),
    mainClass in docker <<= mainClass in docker or mainClass.in(Compile, packageBin),
    dockerfile in docker <<= (managedClasspath in Compile, artifactPath.in(Compile, packageBin), mainClass in docker) map {
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

  /**
   * Sets up all basic docker settings with default values, only [[DockerKeys.dockerfile]] is undefined.
   */
  def dockerSettings: Seq[sbt.Def.Setting[_]] = baseDockerSettings

  /**
   * Sets up all docker settings and defines a simple Dockerfile. The Dockerfile will only expose the specified ports,
   * add all the runtime dependencies and the a packaged artifact of the project and set the entry point to be
   * `java -cp {classpath} {main class}`.
   * The from image defaults to 'dockerfile/java'.
   *
   * These settings will not work for all projects, use [[sbtdocker.Plugin.dockerSettings]] instead and define a
   * [[sbtdocker.Dockerfile]] that works with your project.
   */
  def dockerSettingsAutoPackage(fromImage: String = "dockerfile/java",
                                exposePorts: Seq[Int] = Seq.empty): Seq[sbt.Def.Setting[_]] = {
    baseDockerSettings ++ packageDockerSettings(fromImage, exposePorts)
  }

}
