enablePlugins(DockerPlugin)

name := "scripted-auto-package-default"

organization := "sbtdocker"

version := "0.1.0"

scalaVersion := "2.11.5"

dockerAutoPackageJavaApplication()

val check = taskKey[Unit]("Check")

check := {
  val process = Process("docker", Seq("run", "--rm", "--env", "JAVA_OPTS=-Dmy.system.property=true", (imageNames in docker).value.head.toString))
  val out = process.!!
  if (out.trim != "true") sys.error("Unexpected output: " + out)
}
