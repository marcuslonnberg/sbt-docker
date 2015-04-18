package sbtdocker

import org.scalatest.{FlatSpec, Matchers}
import sbtdocker.Instructions._

class InstructionsSpec extends FlatSpec with Matchers {
  "From" should "create a correct string" in {
    From("image").toString shouldEqual "FROM image"
  }

  "Maintainer" should "create a correct string" in {
    Maintainer("marcus").toString shouldEqual "MAINTAINER marcus"
  }

  "Run" should "create a correct string with exec format" in {
    Run.exec(Seq("echo", "docker")).toString shouldEqual "RUN [\"echo\", \"docker\"]"
    Run.exec(Seq("1 \t3", "\"ö'\\a")).toString shouldEqual "RUN [\"1 \\t3\", \"\\\"\\u00F6'\\\\a\"]"
  }

  it should "create a correct string with shell format" in {
    Run.shell(Seq("echo", "docker")).toString shouldEqual "RUN echo docker"
    Run.shell(Seq("1 \t3", "\"ö'\\a")).toString shouldEqual "RUN 1\\ \\t3 \\\"ö'\\a"
  }

  "Cmd" should "create a correct string with exec format" in {
    Cmd.exec(Seq("cmd", "arg")).toString shouldEqual "CMD [\"cmd\", \"arg\"]"
    Cmd.exec(Seq("1 \t3", "\"ö'\\a")).toString shouldEqual "CMD [\"1 \\t3\", \"\\\"\\u00F6'\\\\a\"]"
  }

  it should "create a correct string" in {
    Cmd.shell(Seq("cmd", "arg")).toString shouldEqual "CMD cmd arg"
    Cmd.shell(Seq("1 \t3", "\"ö'\\a")).toString shouldEqual "CMD 1\\ \\t3 \\\"ö'\\a"
  }

  "Expose" should "create a correct string" in {
    Expose(Seq(80, 8080)).toString shouldEqual "EXPOSE 80 8080"
  }

  "Env" should "create a correct string" in {
    Env("key", "value").toString shouldEqual "ENV key=value"
    Env("key", "-Dconfig.resource=docker.conf").toString shouldEqual """ENV key=-Dconfig.resource\=docker.conf"""
    Env("a", "b=c d&e").toString shouldEqual """ENV a=b\=c\ d&e"""
    Env(Map("key1" -> "value1", "key2" -> "value2")).toString shouldEqual "ENV key1=value1 key2=value2"
    Env("key=value").toString shouldEqual "ENV key=value"
  }

  "Add" should "create a correct string" in {
    AddRaw("a", "b").toString shouldEqual "ADD a b"
  }

  "Copy" should "create a correct string" in {
    CopyRaw("a", "b").toString shouldEqual "COPY a b"
  }

  "EntryPoint" should "create a correct string with exec format" in {
    EntryPoint.exec(Seq("entrypoint", "arg")).toString shouldEqual "ENTRYPOINT [\"entrypoint\", \"arg\"]"
    EntryPoint.exec(Seq("1 \t3", "\"ö'\\a")).toString shouldEqual "ENTRYPOINT [\"1 \\t3\", \"\\\"\\u00F6'\\\\a\"]"
  }

  it should "create a correct string with shell format" in {
    EntryPoint.shell(Seq("entrypoint", "arg")).toString shouldEqual "ENTRYPOINT entrypoint arg"
    EntryPoint.shell(Seq("1 \t3", "\"ö'\\a")).toString shouldEqual "ENTRYPOINT 1\\ \\t3 \\\"ö'\\a"
  }

  "Volume" should "create a correct string" in {
    Volume("mountpoint").toString shouldEqual "VOLUME [\"mountpoint\"]"
  }

  "User" should "create a correct string" in {
    User("marcus").toString shouldEqual "USER marcus"
  }

  "WorkDir" should "create a correct string" in {
    WorkDir("path").toString shouldEqual "WORKDIR path"
  }

  "OnBuild" should "create a correct string" in {
    OnBuild(Run.exec(Seq("echo", "123"))).toString shouldEqual "ONBUILD RUN [\"echo\", \"123\"]"
  }

  "Label" should "create a correct label string" in {
    Label("foo", "bar").toString shouldEqual "LABEL foo=bar"
    Label("com.example.bar", "foo").toString shouldEqual """LABEL com.example.bar=foo"""
    Label("com.example.bar", "foo=bar d&e").toString shouldEqual """LABEL com.example.bar=foo\=bar\ d&e"""
    Label(Map("com.example.bar" -> "foo", "com.example.bor" -> "boz")).toString shouldEqual "LABEL com.example.bar=foo com.example.bor=boz"
    Label("foo=bar").toString shouldEqual "LABEL foo=bar"
  }
}
