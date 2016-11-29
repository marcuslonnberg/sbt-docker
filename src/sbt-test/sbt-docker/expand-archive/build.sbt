import sbtdocker.immutable

enablePlugins(DockerPlugin)

name := "scripted-expand-archive"

organization := "sbtdocker"

version := "0.1.0"

dockerfile in docker := {
  val archive = file("archive.tgz")
  immutable.Dockerfile.empty
    .from("busybox")
    .add(archive, "/")
    .cmd("ls", "dir")
}

val check = taskKey[Unit]("Check")

check := {
  val process = Process("docker", Seq("run", "--rm", (dockerImageNames in docker).value.head.toString))
  val out = process.!!
  if (out.trim != "file") sys.error("Unexpected output: " + out)
}
