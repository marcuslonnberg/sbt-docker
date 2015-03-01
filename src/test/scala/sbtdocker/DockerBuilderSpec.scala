package sbtdocker

import org.scalatest.{FreeSpec, Matchers}
import sbt._

class DockerBuilderSpec extends FreeSpec with Matchers {

  "prepareFiles" - {
    "Add multiple files to different paths" ignore {

    }

    "Add two different files to same destination path" ignore {

    }

    "Add same file twice to same dest" ignore {

    }
  }

  "Build flags" - {
    "Default options should be: use caching, pull if missing and remove on success" in {
      val options = BuildOptions()

      options.cache shouldEqual true
      options.pullBaseImage shouldEqual BuildOptions.Pull.IfMissing
      options.removeIntermediateContainers shouldEqual BuildOptions.Remove.OnSuccess

      val flags = DockerBuilder.buildFlags(options)

      flags should contain theSameElementsAs Seq("--no-cache=false", "--pull=false", "--rm=true")
    }

    "No cache" in {
      val options = BuildOptions(cache = false)
      val flags = DockerBuilder.buildFlags(options)

      flags should contain ("--no-cache=true")
    }

    "Always remove" in {
      val options = BuildOptions(removeIntermediateContainers = BuildOptions.Remove.Always)
      val flags = DockerBuilder.buildFlags(options)

      flags should contain ("--force-rm=true")
    }

    "Never remove" in {
      val options = BuildOptions(removeIntermediateContainers = BuildOptions.Remove.Never)
      val flags = DockerBuilder.buildFlags(options)

      flags should contain ("--pull=false")
    }

    "Always pull" in {
      val options = BuildOptions(pullBaseImage = BuildOptions.Pull.Always)
      val flags = DockerBuilder.buildFlags(options)

      flags should contain ("--pull=true")
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
