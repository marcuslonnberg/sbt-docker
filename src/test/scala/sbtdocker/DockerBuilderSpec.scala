package sbtdocker

import org.scalatest.{FreeSpec, Matchers}
import sbt._
import sbtdocker.Dockerfile.StageDir
import sbtdocker.Instructions.Add

class DockerBuilderSpec extends FreeSpec with Matchers {
  "prepareFiles" - {
    "Add multiple files to different paths" - {
      IO.withTemporaryDirectory { origDir =>
        IO.withTemporaryDirectory { stageDir =>
          val fileA = origDir / "a"
          val fileAData = createFile(fileA)

          val fileB = origDir / "x" / "y" / "z"
          val fileBData = createFile(fileB)

          val fileC = origDir / "x" / "y" / "a"
          val fileCData = createFile(fileC)

          val dockerfile = new Dockerfile {
            add(fileA, "/a/b")
            add(fileB, "/")
            add(fileC, "/x/y")
          }

          DockerBuilder.prepareFiles(dockerfile, StageDir(stageDir), ConsoleLogger())

          val addInstructions = dockerfile.instructions.collect { case add: Add => add}
          addInstructions should have length 3
          val Seq(addA, addB, addC) = addInstructions

          IO.read(stageDir / addA.from) shouldEqual fileAData
          IO.read(stageDir / addB.from) shouldEqual fileBData
          IO.read(stageDir / addC.from) shouldEqual fileCData
        }
      }
    }

    "Add two different files to same destination path" - {
      IO.withTemporaryDirectory { origDir =>
        IO.withTemporaryDirectory { stageDir =>
          val fileA = origDir / "a"
          val fileAData = createFile(fileA)

          val fileB = origDir / "b"
          val fileBData = createFile(fileB)

          val dockerfile = new Dockerfile {
            add(fileA, "/dest")
            // Here could be RUN mv /dest /other/path
            add(fileB, "/dest")
          }

          DockerBuilder.prepareFiles(dockerfile, StageDir(stageDir), ConsoleLogger())

          val addInstructions = dockerfile.instructions.collect { case add: Add => add}
          addInstructions should have length 2
          val Seq(addA, addB) = addInstructions

          IO.read(stageDir / addA.from) shouldEqual fileAData
          IO.read(stageDir / addB.from) shouldEqual fileBData
        }
      }
    }

    "Add same file twice to same dest" - {
      IO.withTemporaryDirectory { origDir =>
        IO.withTemporaryDirectory { stageDir =>
          val fileA = origDir / "a"
          val fileAData = "a"
          assume(fileA.createNewFile())
          IO.write(fileA, fileAData)

          val dockerfile = new Dockerfile {
            add(fileA, "/dest")
            // Here could be RUN mv /dest /other/path
            add(fileA, "/dest")
          }

          DockerBuilder.prepareFiles(dockerfile, StageDir(stageDir), ConsoleLogger())

          val addInstructions = dockerfile.instructions.collect { case add: Add => add}
          addInstructions should have length 2
          val Seq(addFirst, addSecond) = addInstructions

          addFirst shouldEqual addSecond

          IO.read(stageDir / addFirst.from) shouldEqual fileAData
        }
      }
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
