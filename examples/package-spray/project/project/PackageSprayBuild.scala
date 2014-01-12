import sbt._

object PackageSprayBuild extends Build {
  lazy val root = Project("package-spray", file(".")) dependsOn docker
  lazy val docker = file("../..").getAbsoluteFile.toURI
}
