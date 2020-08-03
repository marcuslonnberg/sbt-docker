enablePlugins(DockerPlugin)

name := "scripted-auto-package"

organization := "sbtdocker"

version := "0.1.0"

scalaVersion := "2.12.11"

libraryDependencies += "joda-time" % "joda-time" % "2.7"

javaOptions in docker := Seq("-Dmy.system.property=true")

dockerAutoPackageJavaApplication()

val check = taskKey[Unit]("Check")

check := {
  val process = scala.sys.process.Process("docker", Seq("run", "--rm", (imageNames in docker).value.head.toString))
  val out = process.!!
  if (out.trim != "Hello AutoPackage\n20\ntrue") sys.error("Unexpected output: " + out)
}
