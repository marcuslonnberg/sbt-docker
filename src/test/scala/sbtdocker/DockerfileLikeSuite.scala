package sbtdocker

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sbt.file
import sbtdocker.Instructions._
import sbtdocker.staging.{CopyFile, DefaultDockerfileProcessor, StagedDockerfile}

import scala.concurrent.duration._

class DockerfileLikeSuite extends AnyFunSuite with Matchers {
  val allInstructions = Seq(
    From("image"),
    Maintainer("marcus"),
    Run.exec(Seq("echo","docker")),
    Run.shell(Seq("echo", "docker")),
    Cmd.exec(Seq("cmd", "arg")),
    Cmd.shell(Seq("cmd", "arg")),
    Expose(Seq(80, 8080)),
    Arg("key"),
    Arg("key", Some("defaultValue")),
    Env("key", "value"),
    Label("key", "value"),
    AddRaw("a", "b"),
    CopyRaw("a", "b"),
    EntryPoint.exec(Seq("entrypoint", "arg")),
    EntryPoint.shell(Seq("entrypoint", "arg")),
    Volume("mountpoint"),
    User("marcus"),
    WorkDir("path"),
    OnBuild(Run.exec(Seq("echo", "123"))),
    HealthCheck.exec(Seq("cmd", "arg"), interval = Some(20.seconds), timeout = Some(10.seconds),
      startPeriod = Some(1.second), retries = Some(3)),
    HealthCheck.shell(Seq("cmd", "arg"), interval = Some(20.seconds), timeout = Some(10.seconds),
      startPeriod = Some(1.second), retries = Some(3)),
    HealthCheck.none
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
        |ARG key
        |ARG key="defaultValue"
        |ENV key="value"
        |LABEL key="value"
        |ADD a b
        |COPY a b
        |ENTRYPOINT ["entrypoint", "arg"]
        |ENTRYPOINT entrypoint arg
        |VOLUME ["mountpoint"]
        |USER marcus
        |WORKDIR path
        |ONBUILD RUN ["echo", "123"]
        |HEALTHCHECK --interval=20s --timeout=10s --start-period=1s --retries=3 CMD ["cmd", "arg"]
        |HEALTHCHECK --interval=20s --timeout=10s --start-period=1s --retries=3 CMD cmd arg
        |HEALTHCHECK NONE""".stripMargin
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
      .arg("key")
      .arg("key", Some("defaultValue"))
      .env("key", "value")
      .label("key", "value")
      .addRaw("a", "b")
      .copyRaw("a", "b")
      .entryPoint("entrypoint", "arg")
      .entryPointShell("entrypoint", "arg")
      .volume("mountpoint")
      .user("marcus")
      .workDir("path")
      .onBuild(Run.exec(Seq("echo", "123")))
      .healthCheck(Seq("cmd", "arg"), interval = Some(20.seconds), timeout = Some(10.seconds),
        startPeriod = Some(1.second), retries = Some(3))
      .healthCheckShell(Seq("cmd", "arg"), interval = Some(20.seconds), timeout = Some(10.seconds),
        startPeriod = Some(1.second), retries = Some(3))
      .healthCheckNone()

    withMethods shouldEqual predefined
  }

  test("Run, Cmd, EntryPoint and HealthCheck instructions should handle arguments with whitespace") {
    val dockerfile = immutable.Dockerfile.empty
      .run("echo", "arg \"with\t\nspaces")
      .runShell("echo", "arg \"with\t\nspaces")
      .cmd("echo", "arg \"with\t\nspaces")
      .cmdShell("echo", "arg \"with\t\nspaces")
      .entryPoint("echo", "arg \"with\t\nspaces")
      .entryPointShell("echo", "arg \"with\t\nspaces")
      .healthCheck(Seq("echo", "arg \"with\t\nspaces"))
      .healthCheckShell(Seq("echo", "arg \"with\t\nspaces"))

    staged(dockerfile).instructionsString shouldEqual
      """RUN ["echo", "arg \"with\t\nspaces"]
        |RUN echo arg\ \"with\t\nspaces
        |CMD ["echo", "arg \"with\t\nspaces"]
        |CMD echo arg\ \"with\t\nspaces
        |ENTRYPOINT ["echo", "arg \"with\t\nspaces"]
        |ENTRYPOINT echo arg\ \"with\t\nspaces
        |HEALTHCHECK CMD ["echo", "arg \"with\t\nspaces"]
        |HEALTHCHECK CMD echo arg\ \"with\t\nspaces""".stripMargin
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

  test("Instruction methods with varags should be ignore instruction when zero args") {
    val dockerfile = immutable.Dockerfile.empty
      .run()
      .runShell()
      .cmd()
      .cmdShell()
      .env()
      .expose()
      .entryPoint()
      .entryPointShell()
      .volume()
      .maintainer("test")
      .healthCheck()
      .healthCheckShell()

    dockerfile shouldEqual immutable.Dockerfile.empty.maintainer("test")
  }
}
