import sbt._

object ExampleBuild extends Build {
  lazy val root = Project("example-sbt-native-packager", file(".")) dependsOn docker
  lazy val docker = file("../..").getAbsoluteFile.toURI
}
