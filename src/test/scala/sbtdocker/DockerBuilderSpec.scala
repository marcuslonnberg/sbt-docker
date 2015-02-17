package sbtdocker

import org.scalatest.{FreeSpec, Matchers}
import sbt._
import sbtdocker.Instructions.Add

class DockerBuilderSpec extends FreeSpec with Matchers {
  import sbtdocker.immutable

  "prepareFiles" - {
    "Add multiple files to different paths" ignore {

    }

    "Add two different files to same destination path" ignore {

    }

    "Add same file twice to same dest" ignore {

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
