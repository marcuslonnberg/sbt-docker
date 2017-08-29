package sbtdocker.mutable

import org.scalatest.{FlatSpec, Matchers}
import sbt._
import sbtdocker.ImageName
import sbtdocker.staging.CopyFile

import scala.concurrent.duration._

class MutableDockerfileSpec extends FlatSpec with Matchers {

  import sbtdocker.Instructions._

  "Dockerfile" should "be mutable" in {
    val dockerfile = new Dockerfile()
    dockerfile
      .from("arch")
      .runRaw("echo 123")

    dockerfile.instructions should contain theSameElementsInOrderAs
      Seq(From("arch"), Run("echo 123"))
  }

  it should "have methods for all instructions" in {
    val file1 = file("/1")
    val file2 = file("/2")
    val url1 = new URL("http://domain.tld")
    val url2 = new URL("http://sub.domain.tld")

    val dockerfile = new Dockerfile {
      from("image")
      from(ImageName("ubuntu"))
      maintainer("marcus")
      maintainer("marcus", "marcus@domain.tld")
      run("echo", "1")
      runShell("echo", "2")
      cmd("echo", "1")
      cmdShell("echo", "2")
      expose(80, 443)
      env("key", "value")
      add(file1, "/")
      add(file2, file2)
      addRaw(url1, "/")
      addRaw(url2, file2)
      copy(file1, "/")
      copy(file2, file2)
      entryPoint("echo", "1")
      entryPointShell("echo", "2")
      volume("/srv")
      user("marcus")
      workDir("/srv")
      onBuild(Run.exec(Seq("echo", "text")))
      healthCheck(Seq("healthcheck.sh", "1"), interval = Some(20.seconds), timeout = Some(10.seconds),
        startPeriod = Some(1.second), retries = Some(3))
      healthCheckShell(Seq("healthcheck.sh", "2"), interval = Some(20.seconds), timeout = Some(10.seconds),
        startPeriod = Some(1.second), retries = Some(3))
      healthCheckNone()
    }

    val instructions = Seq(
      From("image"),
      From("ubuntu"),
      Maintainer("marcus"),
      Maintainer("marcus <marcus@domain.tld>"),
      Run.exec(Seq("echo", "1")),
      Run.shell(Seq("echo", "2")),
      Cmd.exec(Seq("echo", "1")),
      Cmd.shell(Seq("echo", "2")),
      Expose(Seq(80, 443)),
      Env("key", "value"),
      Add(Seq(CopyFile(file1)), "/"),
      Add(Seq(CopyFile(file2)), file2.toString),
      AddRaw(url1.toString, "/"),
      AddRaw(url2.toString, file2.toString),
      Copy(Seq(CopyFile(file1)), "/"),
      Copy(Seq(CopyFile(file2)), file2.toString),
      EntryPoint.exec(Seq("echo", "1")),
      EntryPoint.shell(Seq("echo", "2")),
      Volume("/srv"),
      User("marcus"),
      WorkDir("/srv"),
      OnBuild(Run.exec(Seq("echo", "text"))),
      HealthCheck.exec(Seq("healthcheck.sh", "1"), interval = Some(20.seconds), timeout = Some(10.seconds),
        startPeriod = Some(1.second), retries = Some(3)),
      HealthCheck.shell(Seq("healthcheck.sh", "2"), interval = Some(20.seconds), timeout = Some(10.seconds),
        startPeriod = Some(1.second), retries = Some(3)),
      HealthCheckNone)

    dockerfile.instructions should contain theSameElementsInOrderAs instructions
  }
}
