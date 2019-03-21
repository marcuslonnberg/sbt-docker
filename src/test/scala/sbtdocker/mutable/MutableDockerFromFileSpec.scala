package sbtdocker.mutable


import org.scalatest.{FlatSpec, Matchers}
import sbt._
import sbtdocker.Instructions.StageFiles
import sbtdocker.staging.CopyFile

class MutableDockerFromFileSpec extends FlatSpec with Matchers {

  "DockerFromFile" should "be mutable" in {
    val file1 = file("/1")
    val file2 = file("/2")
    val dockerFromfile = new DockerFromFile()
    dockerFromfile
      .from(file1)
      .stageFile(file2, "echo123")

    val sourceFile1 = CopyFile(file1)
    val sourceFile2 = CopyFile(file2)

    dockerFromfile.instructions should contain theSameElementsInOrderAs
      Seq(StageFiles(Seq(sourceFile1), "Dockerfile"), StageFiles(Seq(sourceFile2), "echo123"))
  }

  it should "have methods for all instructions" in {
    val file1 = file("/1")
    val file2 = file("/2")

    val dockerFromfile = new DockerFromFile {
      from(file1)
      stageFile(file2, "echo123")
    }

    val sourceFile1 = CopyFile(file1)
    val sourceFile2 = CopyFile(file2)

    val instructions = Seq(
      StageFiles(Seq(sourceFile1), "Dockerfile"),
      StageFiles(Seq(sourceFile2), "echo123"))

    dockerFromfile.instructions should contain theSameElementsInOrderAs instructions
  }

}
