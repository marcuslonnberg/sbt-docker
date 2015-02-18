package sbtdocker

import org.scalatest.{FunSuite, Matchers}
import sbt.file
import sbtdocker.Instructions._
import staging.{DefaultDockerfileProcessor, StagedDockerfile}

class DockerfileLikeSuite extends FunSuite with Matchers {
  val allInstructions = Seq(
    From("image"),
    Maintainer("marcus"),
    Run.exec(Seq("echo","docker")),
    Run.shell(Seq("echo", "docker")),
    Cmd.exec(Seq("cmd", "arg")),
    Cmd.shell(Seq("cmd", "arg")),
    Expose(Seq(80, 8080)),
    Env("key", "value"),
    AddRaw("a", "b"),
    CopyRaw("a", "b"),
    EntryPoint.exec(Seq("entrypoint", "arg")),
    EntryPoint.shell(Seq("entrypoint", "arg")),
    Volume("mountpoint"),
    User("marcus"),
    WorkDir("path"),
    OnBuild(Run.exec(Seq("echo", "123")))
  )

  test("Instructions string is in correct order and matches instructions") {
    val dockerfile = immutable.Dockerfile(allInstructions)

    staged(dockerfile).instructionsString shouldEqual
      """FROM image
        |MAINTAINER marcus
        |RUN ["echo", "docker"]
        |RUN echo docker
        |CMD ["cmd", "arg"]
        |CMD cmd arg
        |EXPOSE 80 8080
        |ENV key=value
        |ADD a b
        |COPY a b
        |ENTRYPOINT ["entrypoint", "arg"]
        |ENTRYPOINT entrypoint arg
        |VOLUME ["mountpoint"]
        |USER marcus
        |WORKDIR path
        |ONBUILD RUN ["echo", "123"]""".stripMargin
  }

  def staged(dockerfile: immutable.Dockerfile): StagedDockerfile = {
    DefaultDockerfileProcessor(dockerfile, file("/"))
  }

  test("addInstruction changes the Dockerfile by adding a instruction to the end") {
    val predefined = immutable.Dockerfile(allInstructions)

    val withAddInstruction =
      allInstructions.foldLeft(immutable.Dockerfile.empty) {
        case (dockerfile, instruction) => dockerfile.addInstruction(instruction)
      }

    withAddInstruction shouldEqual predefined
  }

  test("Instruction methods adds a instruction to the dockerfile") {
    val predefined = immutable.Dockerfile(allInstructions)

    val withMethods = immutable.Dockerfile.empty
      .from("image")
      .maintainer("marcus")
      .run("echo", "docker")
      .runShell("echo", "docker")
      .cmd("cmd", "arg")
      .cmdShell("cmd", "arg")
      .expose(80, 8080)
      .env("key", "value")
      .addRaw("a", "b")
      .copyRaw("a", "b")
      .entryPoint("entrypoint", "arg")
      .entryPointShell("entrypoint", "arg")
      .volume("mountpoint")
      .user("marcus")
      .workDir("path")
      .onBuild(Run.exec(Seq("echo", "123")))

    withMethods shouldEqual predefined
  }

  test("Run, Cmd and EntryPoint instructions should handle arguments with whitespace") {
    val dockerfile = immutable.Dockerfile.empty
      .run("echo", "arg \"with\t\nspaces")
      .runShell("echo", "arg \"with\t\nspaces")
      .cmd("echo", "arg \"with\t\nspaces")
      .cmdShell("echo", "arg \"with\t\nspaces")
      .entryPoint("echo", "arg \"with\t\nspaces")
      .entryPointShell("echo", "arg \"with\t\nspaces")

    staged(dockerfile).instructionsString shouldEqual
      """RUN ["echo", "arg \"with\t\nspaces"]
        |RUN echo arg\ \"with\t\nspaces
        |CMD ["echo", "arg \"with\t\nspaces"]
        |CMD echo arg\ \"with\t\nspaces
        |ENTRYPOINT ["echo", "arg \"with\t\nspaces"]
        |ENTRYPOINT echo arg\ \"with\t\nspaces""".stripMargin
  }

  test("Add and copy a file to /") {
    val sourceFile = file("/tmp/abc/xyz/")
    val dockerfile = immutable.Dockerfile.empty
      .add(sourceFile, "/")
      .copy(sourceFile, "/")

    dockerfile.instructions should contain theSameElementsInOrderAs Seq(
      Add(Seq(CopyFile(sourceFile)), "/"),
      Copy(Seq(CopyFile(sourceFile)), "/"))
  }

  test("Adding a single source file to multiple paths") {
    val sourceFile = file("/a/b/c/d")
    val dockerfile = immutable.Dockerfile.empty
      .add(sourceFile, "/x/y")
      .add(sourceFile, "/z")
      .add(sourceFile, "/z")
      .copy(sourceFile, "/x/y")
      .copy(sourceFile, "/z")
      .copy(sourceFile, "/z")

    dockerfile.instructions should contain theSameElementsInOrderAs Seq(
      Add(Seq(CopyFile(sourceFile)), "/x/y"),
      Add(Seq(CopyFile(sourceFile)), "/z"),
      Add(Seq(CopyFile(sourceFile)), "/z"),
      Copy(Seq(CopyFile(sourceFile)), "/x/y"),
      Copy(Seq(CopyFile(sourceFile)), "/z"),
      Copy(Seq(CopyFile(sourceFile)), "/z"))
  }

  // TODO: move
  test("OnBuild instruction should correctly generate instruction string") {
    val onBuild = OnBuild(Run.exec(Seq("echo", "123")))

    onBuild.toString shouldEqual """ONBUILD RUN ["echo", "123"]"""
  }

  // TODO: move
  test("Run, EntryPoint and Cmd should support shell format") {
    Run.shell(Seq("a", "b", "\"c\"")).toString shouldEqual """RUN a b \"c\""""
    EntryPoint.shell(Seq("a", "b", "\"c\"")).toString shouldEqual """ENTRYPOINT a b \"c\""""
    Cmd.shell(Seq("a", "b", "\"c\"")).toString shouldEqual """CMD a b \"c\""""
  }

  // TODO: move
  test("Run, EntryPoint and Cmd should support exec format") {
    Run.exec(Seq("a", "b", "\"c\"")).toString shouldEqual """RUN ["a", "b", "\"c\""]"""
    EntryPoint.exec(Seq("a", "b", "\"c\"")).toString shouldEqual """ENTRYPOINT ["a", "b", "\"c\""]"""
    Cmd.exec(Seq("a", "b", "\"c\"")).toString shouldEqual """CMD ["a", "b", "\"c\""]"""
  }
}
