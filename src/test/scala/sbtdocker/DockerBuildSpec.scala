package sbtdocker

import java.io.File

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import sbt.IO
import sbtdocker.Helpers._
import sbtdocker.Instructions._
import sbtdocker.staging.{CopyFile, StagedDockerfile}

class DockerBuildSpec extends AnyFreeSpec with Matchers {

  "Stage files" - {
    "Files should be staged" in {
      IO.withTemporaryDirectory { origDir =>
        IO.withTemporaryDirectory { stageDir =>
          val fileA = origDir / "fileA"
          val fileAData = createFile(fileA)

          val stagedDockerfile = StagedDockerfile(
            instructions = Seq.empty,
            stageFiles = Set(
              CopyFile(fileA) -> (stageDir / "fileA")
            )
          )
          DockerBuild.prepareFiles(stagedDockerfile)

          IO.read(stageDir / "fileA") shouldEqual fileAData
        }
      }
    }

    "A Dockerfile should be created" in {
      IO.withTemporaryDirectory { stageDir =>
        val stagedDockerfile = StagedDockerfile(
          instructions = Seq(
            From("ubuntu"),
            Run("echo 123")
          ),
          stageFiles = Set.empty
        )
        DockerBuild.createDockerfile(stagedDockerfile, stageDir)

        val file = stageDir / "Dockerfile"
        file.exists() shouldEqual true
        IO.read(file) shouldEqual "FROM ubuntu\nRUN echo 123"
      }
    }

    def createFile(file: File): String = {
      val fileData = file.getPath
      file.getParentFile.mkdirs()
      assume(file.createNewFile())
      IO.write(file, fileData)
      fileData
    }
  }

  "Build flags" - {
    "Default options should be: use caching, pull if missing and remove on success" in {
      val options = BuildOptions()

      options.cache shouldEqual true
      options.pullBaseImage shouldEqual BuildOptions.Pull.IfMissing
      options.removeIntermediateContainers shouldEqual BuildOptions.Remove.OnSuccess

      val flags = DockerBuild.generateBuildOptionFlags(options)

      flags should contain theSameElementsAs Seq("--no-cache=false", "--pull=false", "--rm=true")
    }

    "No cache" in {
      val options = BuildOptions(cache = false)
      val flags = DockerBuild.generateBuildOptionFlags(options)

      flags should contain("--no-cache=true")
    }

    "Always remove" in {
      val options = BuildOptions(removeIntermediateContainers = BuildOptions.Remove.Always)
      val flags = DockerBuild.generateBuildOptionFlags(options)

      flags should contain("--force-rm=true")
    }

    "Never remove" in {
      val options = BuildOptions(removeIntermediateContainers = BuildOptions.Remove.Never)
      val flags = DockerBuild.generateBuildOptionFlags(options)

      flags should contain("--pull=false")
    }

    "Always pull" in {
      val options = BuildOptions(pullBaseImage = BuildOptions.Pull.Always)
      val flags = DockerBuild.generateBuildOptionFlags(options)

      flags should contain("--pull=true")
    }

    "Custom arguments" in {
      val options = BuildOptions(otherArguments = Seq("--add-host", "127.0.0.1:12345", "--compress"))
      val flags = DockerBuild.generateBuildOptionFlags(options)

      flags should contain inOrderElementsOf Seq("--add-host", "127.0.0.1:12345", "--compress")
    }
  }
}
