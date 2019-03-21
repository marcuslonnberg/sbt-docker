package sbtdocker.staging

import org.scalatest.{FlatSpec, Matchers}
import sbt.file
import sbtdocker.ImmutableDockerFromFile
import sbtdocker.Helpers._

class DefaultDockerFromFileProcessorSpec extends FlatSpec with Matchers {
  val stageDir = file("/tmp/staging")

  "The default Dockerfile processor" should "handle file staging instructions" in {
    val dockerSourcefile = ImmutableDockerFromFile.empty
      .from(file("Dockerfile"))
      .stageFile(file("/a/b/c"), "/b/c")

    val stagedDockerfile = DefaultDockerFromFileProcessor(dockerSourcefile, stageDir)

    stagedDockerfile.instructions shouldBe empty
    stagedDockerfile.instructionsString shouldBe empty
    stagedDockerfile.stageFiles should contain theSameElementsAs Seq(
      CopyFile(file("/a/b/c")) -> (stageDir / "b" / "c"),
      CopyFile(file("Dockerfile")) -> (stageDir / "Dockerfile")
    )
  }

  it should "handle staging many files" in {
    val dockerSourcefile = ImmutableDockerFromFile.empty
      .stageFile(file("/a/b/c"), "/b/c")
      .stageFile(file("dir"), "dir")
      .stageFile(file("/other/file"), "/x/")

    val stagedDockerfile = DefaultDockerFromFileProcessor(dockerSourcefile, stageDir)

    stagedDockerfile.stageFiles should contain theSameElementsAs Seq(
      CopyFile(file("/a/b/c")) -> (stageDir / "b" / "c"),
      CopyFile(file("dir")) -> (stageDir / "dir"),
      CopyFile(file("/other/file")) -> (stageDir / "x" / "file" )
    )
  }
}
