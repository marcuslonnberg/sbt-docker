import DockerKeys._

name := "scripted-auto-package"

organization := "sbtdocker"

version := "0.1.0"

scalaVersion := "2.11.1"

dockerSettingsAutoPackage()

val check = taskKey[Unit]("Check")

check := {
  val process = Process("docker", Seq("run", (imageName in docker).value.name))
  val out = process.!!
  if (out.trim != "Hello AutoPackage") sys.error("Unexpected output: " + out)
}
