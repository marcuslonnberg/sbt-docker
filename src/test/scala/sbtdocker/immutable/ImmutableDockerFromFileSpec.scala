package sbtdocker.immutable

import org.scalatest.{FlatSpec, Matchers}
import sbt.file
import sbtdocker.Instructions.StageFiles
import sbtdocker.staging.CopyFile

class ImmutableDockerFromFileSpec extends FlatSpec with Matchers {

  "DockerFromfile" should "be immutable" in {
    val empty = DockerFromFile()
    val nonEmpty = empty
      .stageFile(file("/x"), "/")

    empty should not equal nonEmpty
  }

  it should "have methods for all instructions" in {
    val file1 = file("/1")
    val file2 = file("/2")

    val dockerFromFile = DockerFromFile.empty
      .from(file1)
      .stageFile(file2, "file2")

    val sourceFile1 = CopyFile(file1)
    val sourceFile2 = CopyFile(file2)


    val instructions = Seq(
      StageFiles(Seq(sourceFile1), "Dockerfile"),
      StageFiles(Seq(sourceFile2), "file2"))

    dockerFromFile.instructions should contain theSameElementsInOrderAs instructions
  }

}
