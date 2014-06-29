package sbtdocker.immutable

import java.net.URL

import sbt.file
import org.scalatest.{FlatSpec, Matchers}
import sbtdocker.{Dockerfile, ImageName, Instructions}
import sbtdocker.Instructions._

class ImmutableDockerfileSuite extends FlatSpec with Matchers {
  "A Dockerfile" should "be immutable" in {
    val empty = Dockerfile()
    val nonEmpty = empty
      .run("echo")
      .add(file("/x"), "/")

    empty should not equal nonEmpty
  }

  it should "have methods for all instructions" in {
    val file1 = file("/1")
    val file2 = file("/2")
    val url1 = new URL("http://domain.tld")
    val url2 = new URL("http://sub.domain.tld")

    val dockerfile = Dockerfile.empty
      .from("image")
      .from(ImageName("ubuntu"))
      .maintainer("marcus")
      .maintainer("marcus", "marcus@domain.tld")
      .run("echo", "1")
      .runShell("echo", "2")
      .cmd("echo", "1")
      .cmdShell("echo", "2")
      .expose(80, 443)
      .env("key", "value")
      .add(file1, "/")
      .add(file2, file2)
      .add(url1, "/")
      .add(url2, file2)
      .copy(file1, "/")
      .copy(file2, file2)
      .copy(url1, "/")
      .copy(url2, file2)
      .entryPoint("echo", "1")
      .entryPointShell("echo", "2")
      .volume("/srv")
      .user("marcus")
      .workDir("/srv")
      .onBuild(Instructions.Run("echo", "text"))

    val instructions = Seq(From("image"),
      From("ubuntu"),
      Maintainer("marcus"),
      Maintainer("marcus <marcus@domain.tld>"),
      Run("echo", "1"),
      Run.shell("echo", "2"),
      Cmd("echo", "1"),
      Cmd.shell("echo", "2"),
      Expose(80, 443),
      Env("key", "value"),
      Add(file1.toString, "/"),
      Add(file2.toString, file2.toString),
      Add(url1.toString, "/"),
      Add(url2.toString, file2.toString),
      Copy(file1.toString, "/"),
      Copy(file2.toString, file2.toString),
      Copy(url1.toString, "/"),
      Copy(url2.toString, file2.toString),
      EntryPoint("echo", "1"),
      EntryPoint.shell("echo", "2"),
      Volume("/srv"),
      User("marcus"),
      WorkDir("/srv"),
      OnBuild(Instructions.Run("echo", "text")))

    dockerfile.instructions shouldEqual instructions
  }
}
