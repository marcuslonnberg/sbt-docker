package sbtdocker

import sbt.Logger

import sys.process.{Process, ProcessLogger}
import scala.util.parsing.combinator.RegexParsers

case class DockerVersion(major: Int, minor: Int, release: Int)

object DockerVersion extends RegexParsers {
  def apply(dockerPath: String, log: Logger): DockerVersion = {
    val processLogger = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    val command = dockerPath :: "version" :: "-f" :: "{{.Server.Version}}" :: Nil
    val processOutput = Process(command).!!(processLogger)
    parseVersion(processOutput)
  }

  def parseVersion(version: String): DockerVersion = {
    parse(parser, version) match {
      case Success(ver, _) => ver
      case NoSuccess(msg, _) => throw new RuntimeException(s"Could not parse Version from $version: $msg")
    }
  }

  private val positiveWholeNumber: Parser[Int] = {
    """\d+""".r.map(_.toInt).withFailureMessage("non-negative integer value expected")
  }

  private val parser: Parser[DockerVersion] = {
    positiveWholeNumber ~ ("." ~> positiveWholeNumber) ~ ("." ~> positiveWholeNumber) ^^ {
      case major ~ minor ~ release  => DockerVersion(major, minor, release)
    }
  }
}