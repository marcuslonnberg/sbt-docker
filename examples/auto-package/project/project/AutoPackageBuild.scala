import sbt._

object AutoPackageBuild extends Build {
  lazy val root = Project("auto-package", file(".")) dependsOn docker
  lazy val docker = file("../..").getAbsoluteFile.toURI
}
