name := "scripted-auto-package"

organization := "sbtdocker"

version := "0.1.0"

scalaVersion := "2.11.4"

dockerAutoPackage()

val check = taskKey[Unit]("Check")

check := {
  val process = Process("docker", Seq("run", (imageName in docker).value.toString))
  val out = process.!!
  if (out.trim != "Hello AutoPackage") sys.error("Unexpected output: " + out)
}
