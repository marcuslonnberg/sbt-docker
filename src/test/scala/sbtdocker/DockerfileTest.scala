package sbtdocker

import sbt.IO
import sbt.file
import sbt.Path._
import Instructions._
import org.scalatest.{FunSuite, Matchers}

class DockerfileTest extends FunSuite with Matchers {
  val allInstructions = Seq(
    From("image"),
    Maintainer("marcus"),
    Run("echo", "docker"),
    Cmd("cmd", "arg"),
    Expose(80, 8080),
    Env("key", "value"),
    Add("a", "b"),
    EntryPoint("entrypoint", "arg"),
    Volume("mountpoint"),
    User("marcus"),
    WorkDir("path"),
    OnBuild(Run("echo", "123"))
  )

  test("Instructions string is in sequence and matches instructions") {
    val dockerfile = Dockerfile(allInstructions)

    dockerfile.toInstructionsString shouldEqual
      """FROM image
        |MAINTAINER marcus
        |RUN ["echo", "docker"]
        |CMD ["cmd", "arg"]
        |EXPOSE 80 8080
        |ENV key value
        |ADD a b
        |ENTRYPOINT ["entrypoint", "arg"]
        |VOLUME mountpoint
        |USER marcus
        |WORKDIR path
        |ONBUILD RUN ["echo", "123"]""".stripMargin
  }

  test("addInstruction changes the Dockerfile by adding a instruction to the end") {
    val predefined = Dockerfile(allInstructions)

    val withAddInstruction = new Dockerfile {
      allInstructions foreach addInstruction
    }

    withAddInstruction shouldEqual predefined
  }

  test("Instruction methods adds a instruction to the dockerfile") {
    val predefined = Dockerfile(allInstructions)

    val withMethods = new Dockerfile {
      from("image")
      maintainer("marcus")
      run("echo", "docker")
      cmd("cmd", "arg")
      expose(80, 8080)
      env("key", "value")
      add("a", "b")
      entryPoint("entrypoint", "arg")
      volume("mountpoint")
      user("marcus")
      workDir("path")
      onBuild(Run("echo", "123"))
    }

    withMethods shouldEqual predefined
  }

  test("add method should create a from path that is relative to the stage dir") {
    IO.withTemporaryDirectory {
      tempDir =>
        val d = new Dockerfile {
          implicit val stageDir = tempDir
          add(tempDir / "b.txt", file("c.txt"))
        }
        d.instructions should contain theSameElementsAs Seq(Add("b.txt", "c.txt"))
        d.pathsToCopy shouldBe empty
    }

    val d2 = new Dockerfile {
      implicit val stageDir = file("/e")
      add(file("/a/b/c/d.txt"), file("/c/d.txt"))
    }
    d2.instructions should contain theSameElementsAs Seq(Add("d.txt", "/c/d.txt"))
    d2.pathsToCopy should have length 1
  }

  test("add method should not create a relative path of the from path when there is no stage dir defined") {
    val d = new Dockerfile {
      add(file("/a/b/c/d.txt"), file("/c/d.txt"))
    }
    d.instructions should contain theSameElementsAs Seq(Add("a/b/c/d.txt", "/c/d.txt"))
    d.pathsToCopy shouldBe empty
  }

  test("OnBuild instruction should correctly generate instruction string") {
    val onBuild = OnBuild(Run("echo", "123"))

    onBuild.toInstructionString shouldEqual """ONBUILD RUN ["echo", "123"]"""
  }

  test("Run, EntryPoint and Cmd should support shell format") {
    Run.shell("a", "b", "\"c\"").toInstructionString shouldEqual """RUN a b "c""""
    EntryPoint.shell("a", "b", "\"c\"").toInstructionString shouldEqual """ENTRYPOINT a b "c""""
    Cmd.shell("a", "b", "\"c\"").toInstructionString shouldEqual """CMD a b "c""""
  }

  test("Run, EntryPoint and Cmd should support exec format") {
    Run("a", "b", "\"c\"").toInstructionString shouldEqual """RUN ["a", "b", "\"c\""]"""
    EntryPoint("a", "b", "\"c\"").toInstructionString shouldEqual """ENTRYPOINT ["a", "b", "\"c\""]"""
    Cmd("a", "b", "\"c\"").toInstructionString shouldEqual """CMD ["a", "b", "\"c\""]"""
  }
}
