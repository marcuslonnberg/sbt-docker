package sbtdocker

import sbt.Keys._
import sbt._
import sbtdocker.DockerKeys._
import sbtdocker.Instructions.Add

object DockerSettings {
  lazy val baseDockerSettings = Seq(
    docker <<= buildDockerImage(docker),
    baseDockerImage <<= buildDockerImage(baseDockerImage),
    dockerPush := {
      val log = Keys.streams.value.log
      val dockerCmd = (DockerKeys.dockerCmd in docker).value
      val imageName = (DockerKeys.imageName in docker).value

      DockerPush(dockerCmd, imageName, log)
    },
    dockerBuildAndPush <<= (docker, dockerPush) { (build, push) =>
      build.flatMap { id =>
        push.map(_ => id)
      }
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
    dockerfile in baseDockerImage := {
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
    imageName in baseDockerImage := {
      val organisation = Option(Keys.organization.value).filter(_.nonEmpty)
      val name = Keys.normalizedName.value + "-base"
      ImageName(namespace = organisation, repository = name)
    },
    dockerCmd := sys.env.get("DOCKER").filter(_.nonEmpty).getOrElse("docker"),
    buildOptions in docker := BuildOptions(),
    buildOptions in baseDockerImage := BuildOptions()
  )

  def buildDockerImage(dockerTask: TaskKey[_]): Def.Initialize[Task[ImageId]] = Def.task {
    val log = Keys.streams.value.log
    val dockerCmd = DockerKeys.dockerCmd.value
    val buildOptions = (DockerKeys.buildOptions in dockerTask).value
    val stageDir = (target in docker).value
    val dockerfile = (DockerKeys.dockerfile in dockerTask).value
    val imageName = (DockerKeys.imageName in dockerTask).value
    val cacheDir = target.value / s"docker-image-cache" / imageName.toString
    val cacheFile = cacheDir/ name.value

    log.debug("Dockerfile:")
    log.debug(dockerfile.mkString)

    /* Wrap actual function in a function that provides basic caching . */
    val cachedFun = FileFunction.cached(cacheDir, FilesInfo.lastModified, FilesInfo.exists) {
      (inFiles: Set[File]) => {
        val imageId = DockerBuilder(dockerCmd, buildOptions, imageName, dockerfile, stageDir, log)
        IO.write(cacheFile, imageId.id)
        Set(cacheFile)
      }
    }

    /** Recursively list all directories so caching will detect changes in subdirectories */
    def findDirs(file: File): Seq[File] = file.isDirectory match {
      case true => file +: file.listFiles().flatMap(findDirs)
      case false => Nil
    }

    // Normalize instructions that include our temporary tgz files since these change every run.
    // Changes to these files will be detected by changes to StagedArchive directories.
    val userInstructions = dockerfile.instructions.map {
      case Add(src, dest) if src.contains("dockerbuild") => Add("archive", dest)
      case other => other
    }

    // To detect changes in the docker file itself we create a fake file based on the hash
    // of the docker file description.
    // TODO: use a better hash function here...
    val fakeDockerFile = target.value / (userInstructions.hashCode.toString + ".docker.hash")
    log.debug(s"dockerfile caching hashcode: $fakeDockerFile")

    val stagedArchives = dockerfile.stagedArchives.flatMap(d => findDirs(d.file))
    val stagedFiles = dockerfile.stagedFiles.map(_.source)
    val fileDependencies = (Seq(fakeDockerFile) ++ stagedArchives ++ stagedFiles).toSet

    ImageId(IO.read(cachedFun(fileDependencies).head))
  }

  def packageDockerSettings(fromImage: String, exposePorts: Seq[Int]) = Seq(
    docker <<= docker.dependsOn(Keys.`package`.in(Compile, Keys.packageBin)),
    Keys.mainClass in docker <<= Keys.mainClass in docker or Keys.mainClass.in(Compile, Keys.packageBin),
    dockerfile in docker <<= (Keys.managedClasspath in Compile, Keys.artifactPath.in(Compile, Keys.packageBin), Keys.mainClass in docker) map {
      case (_, _, None) =>
        sys.error("No main class found or multiple main classes exists. " +
          "One can be set with 'mainClass in docker := Some(\"package.MainClass\")'.")
      case (classpath, artifact, Some(mainClass)) =>
        val appPath = "/app"
        val libsPath = s"$appPath/libs/"
        val artifactPath = s"$appPath/${artifact.name}"

        val dockerfile = Dockerfile()
        dockerfile.from(fromImage)

        if (exposePorts.nonEmpty) {
          dockerfile.expose(exposePorts: _*)
        }

        val libPaths = classpath.files.map { libFile =>
          val toPath = file(libsPath) / libFile.name
          dockerfile.stageFile(libFile, toPath)
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
