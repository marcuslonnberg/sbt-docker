package sbtdocker.staging

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt.file
import sbtdocker.Helpers._
import sbtdocker.ImmutableDockerfile
import sbtdocker.Instructions.{AddRaw, CopyRaw, From, Run}

class DefaultDockerfileProcessorSpec extends AnyFlatSpec with Matchers {
  val stageDir = file("/tmp/staging")

  "The default Dockerfile processor" should "handle non file instructions" in {
    val dockerfile = ImmutableDockerfile.empty
      .from("ubuntu")
      .runRaw("echo hello")

    val stagedDockerfile = DefaultDockerfileProcessor(dockerfile, stageDir)

    stagedDockerfile.instructions should contain theSameElementsInOrderAs Seq(
      From("ubuntu"),
      Run("echo hello")
    )
    stagedDockerfile.instructionsString shouldEqual
      """FROM ubuntu
        |RUN echo hello""".stripMargin
    stagedDockerfile.stageFiles shouldBe empty
  }

  it should "handle file staging instructions" in {
    val dockerfile = ImmutableDockerfile.empty
      .stageFile(file("/a/b/c"), "/b/c")

    val stagedDockerfile = DefaultDockerfileProcessor(dockerfile, stageDir)

    stagedDockerfile.instructions shouldBe empty
    stagedDockerfile.instructionsString shouldBe empty
    stagedDockerfile.stageFiles should contain theSameElementsAs Seq(
      CopyFile(file("/a/b/c")) -> (stageDir / "b" / "c")
    )
  }

  it should "handle file staging docker instructions" in {
    val dockerfile = ImmutableDockerfile.empty
      .add(file("/a/b/c"), "/b/c")

    val stagedDockerfile = DefaultDockerfileProcessor(dockerfile, stageDir)

    stagedDockerfile.instructions should contain theSameElementsAs Seq(
      AddRaw("0/c", "/b/c")
    )
    stagedDockerfile.instructionsString shouldEqual "ADD 0/c /b/c"
    stagedDockerfile.stageFiles should contain theSameElementsAs Seq(
      CopyFile(file("/a/b/c")) -> (stageDir / "0" / "c")
    )
  }

  it should "handle staging many files" in {
    val dockerfile = ImmutableDockerfile.empty
      .copy(file("/a/b/c"), "/b/c")
      .stageFile(file("dir"), "dir")
      .add(Seq(file("/file1"), file("/file2")), "/x/")
      .add(file("/other/file"), "/x/")

    val stagedDockerfile = DefaultDockerfileProcessor(dockerfile, stageDir)

    stagedDockerfile.instructions should contain theSameElementsAs Seq(
      CopyRaw("0/c", "/b/c"),
      AddRaw(Seq("2/file1", "3/file2"), "/x/"),
      AddRaw("4/file", "/x/")
    )
    stagedDockerfile.instructionsString shouldEqual
      """COPY 0/c /b/c
        |ADD 2/file1 3/file2 /x/
        |ADD 4/file /x/""".stripMargin
    stagedDockerfile.stageFiles should contain theSameElementsAs Seq(
      CopyFile(file("/a/b/c")) -> (stageDir / "0" / "c"),
      CopyFile(file("dir")) -> (stageDir / "dir"),
      CopyFile(file("/file1")) -> (stageDir / "2" / "file1"),
      CopyFile(file("/file2")) -> (stageDir / "3" / "file2"),
      CopyFile(file("/other/file")) -> (stageDir / "4" / "file")
    )
  }
}
