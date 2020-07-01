package sbtdocker.staging

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt._
import sbtdocker.Instructions.{Expose, From, Maintainer}

class StagedDockerfileSpec extends AnyFlatSpec with Matchers {
  "A staged Dockerfile" should "be empty from the start" in {
    val sdf = StagedDockerfile.empty
    sdf.instructions shouldBe empty
    sdf.stageFiles shouldBe empty
  }

  it should "immutable" in {
    val sdf = StagedDockerfile.empty
    sdf.addInstruction(From("abc"))
    sdf.stageFile(CopyFile(file("s1")), file("d1"))

    sdf.instructions shouldBe empty
    sdf.stageFiles shouldBe empty
  }

  it should "contain stage files" in {
    val sdf = StagedDockerfile.empty
      .stageFile(CopyFile(file("s1")), file("d1"))
      .stageFile(CopyFile(file("s2")), file("d2"))

    sdf.stageFiles should contain theSameElementsAs Seq(
      CopyFile(file("s1")) -> file("d1"),
      CopyFile(file("s2")) -> file("d2")
    )
  }

  it should "contain instructions" in {
    val sdf = StagedDockerfile.empty
      .addInstruction(From("abc"))
      .addInstruction(Maintainer("xyz"))

    sdf.instructions should contain theSameElementsInOrderAs Seq(
      From("abc"),
      Maintainer("xyz")
    )
  }

  it should "generate a instructions string" in {
    val sdf = StagedDockerfile.empty
      .addInstruction(From("abc"))
      .addInstruction(Maintainer("xyz"))
      .addInstruction(Expose(Seq(80)))

    sdf.instructionsString shouldEqual "FROM abc\nMAINTAINER xyz\nEXPOSE 80"
  }
}
