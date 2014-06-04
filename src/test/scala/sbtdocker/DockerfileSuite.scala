package sbtdocker

import sbt.file
import sbt.Path._
import Instructions._
import org.scalatest.{FunSuite, Matchers}
import sbtdocker.Dockerfile.CopyPath

class DockerfileSuite extends FunSuite with Matchers {
  val allInstructions = Seq(
    From("image"),
    Maintainer("marcus"),
    Run("echo", "docker"),
    Run.shell("echo", "docker"),
    Cmd("cmd", "arg"),
    Cmd.shell("cmd", "arg"),
    Expose(80, 8080),
    Env("key", "value"),
    Add("a", "b"),
    EntryPoint("entrypoint", "arg"),
    EntryPoint.shell("entrypoint", "arg"),
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
        |RUN echo docker
        |CMD ["cmd", "arg"]
        |CMD cmd arg
        |EXPOSE 80 8080
        |ENV key value
        |ADD a b
        |ENTRYPOINT ["entrypoint", "arg"]
        |ENTRYPOINT entrypoint arg
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
      runShell("echo", "docker")
      cmd("cmd", "arg")
      cmdShell("cmd", "arg")
      expose(80, 8080)
      env("key", "value")
      add("a", "b")
      entryPoint("entrypoint", "arg")
      entryPointShell("entrypoint", "arg")
      volume("mountpoint")
      user("marcus")
      workDir("path")
      onBuild(Run("echo", "123"))
    }

    withMethods shouldEqual predefined
  }

  test("Add a file to /") {
    val stageDir = file("/tmp/abc/xyz/")
    val sourceFile = stageDir / "xyz"
    val dockerfile = new Dockerfile {
      add(sourceFile, "/")
    }

    dockerfile.instructions should contain theSameElementsAs Seq(Add("/xyz", "/xyz"))
  }

  test("Add a file in the root") {
    val sourceFile = file("/tmp/xyz")
    val d = new Dockerfile {
      add(sourceFile, "abc")
    }
    d.instructions should contain theSameElementsAs Seq(Add("abc", "abc"))
    d.pathsToCopy should contain theSameElementsAs Seq(CopyPath(sourceFile, file("abc")))
  }

  test("Adding a single source file to multiple paths") {
    val sourceFile = file("/a/b/c/d")
    val dockerfile = new Dockerfile {
      add(sourceFile, "/x/y")
      add(sourceFile, "/z")
      add(sourceFile, "/z")
    }

    dockerfile.instructions should contain theSameElementsInOrderAs Seq(
      Add("/x/y", "/x/y"),
      Add("/z", "/z"),
      Add("/z", "/z"))
    dockerfile.pathsToCopy should contain theSameElementsInOrderAs Seq(
      CopyPath(sourceFile, file("/x/y")),
      CopyPath(sourceFile, file("/z")),
      CopyPath(sourceFile, file("/z")))
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
