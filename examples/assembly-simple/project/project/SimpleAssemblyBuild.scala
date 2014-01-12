import sbt._

object SimpleAssemblyBuild extends Build {
  lazy val root = Project("assembly-simple", file(".")) dependsOn docker
  lazy val docker = file("../..").getAbsoluteFile.toURI
}
