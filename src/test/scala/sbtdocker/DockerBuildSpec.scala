package sbtdocker

import com.spotify.docker.client.DockerClient.BuildParam
import org.scalatest.{FreeSpec, Matchers}
import sbt.{File, IO, richFile}
import sbtdocker.Instructions._
import sbtdocker.staging.StagedDockerfile

class DockerBuildSpec extends FreeSpec with Matchers {

  "Stage files" - {
    "Files should be staged" in {
      IO.withTemporaryDirectory { origDir =>
        IO.withTemporaryDirectory { stageDir =>
          val fileA = origDir / "fileA"
          val fileAData = createFile(fileA)

          val dockerfile = new Dockerfile {
            copy(fileA, "fileA")
          }

          DockerStage(stageDir, dockerfile)

          IO.read(stageDir / "0" / "fileA") shouldEqual fileAData
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
        DockerStage(stageDir, stagedDockerfile)

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

      val flags = DockerClientHelpers.buildParams(options)

      flags should contain theSameElementsAs Seq(BuildParam.rm(true))
    }

    "No cache" in {
      val options = BuildOptions(cache = false)
      val flags = DockerClientHelpers.buildParams(options)

      flags should contain(BuildParam.noCache())
    }

    "Always remove" in {
      val options = BuildOptions(removeIntermediateContainers = BuildOptions.Remove.Always)
      val flags = DockerClientHelpers.buildParams(options)

      flags should contain(BuildParam.forceRm())
    }

    "Never remove" in {
      val options = BuildOptions(removeIntermediateContainers = BuildOptions.Remove.Never)
      val flags = DockerClientHelpers.buildParams(options)

      flags should not contain BuildParam.noCache()
    }

    "Always pull" in {
      val options = BuildOptions(pullBaseImage = BuildOptions.Pull.Always)
      val flags = DockerClientHelpers.buildParams(options)

      flags should contain(BuildParam.pullNewerImage())
    }
  }
}
