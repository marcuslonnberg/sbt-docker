import sbt._

object ExampleBuild extends Build {
  lazy val root = Project("example-sbt-assembly", file(".")) dependsOn docker
  lazy val docker = file("../..").getAbsoluteFile.toURI
}
