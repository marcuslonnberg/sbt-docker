import sbt._

object SimplePackageBuild extends Build {
  lazy val root = Project("package-simple", file(".")) dependsOn docker
  lazy val docker = file("../..").getAbsoluteFile.toURI
}
