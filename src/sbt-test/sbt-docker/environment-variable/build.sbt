enablePlugins(DockerPlugin)

name := "environment-variable"

organization := "sbtdocker"

version := "0.1.0"

val environmentValue = "b=c 'd|!@#$%^&*(\")e"

docker / dockerfile := {
  new Dockerfile {
    from("busybox")
    env("a"-> environmentValue, "b" -> "value")
    entryPointRaw("echo $a")
  }
}

val check = taskKey[Unit]("Check")
check := {
  val process = scala.sys.process.Process("docker", Seq("run", "--rm", (docker / imageNames).value.head.toString))
  val out = process.!!
  if (out.trim != environmentValue) sys.error("Unexpected output: " + out)
}
