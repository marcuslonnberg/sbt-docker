enablePlugins(DockerPlugin)

name := "check-label"

organization := "sbtdocker"

version := "0.1.0"

dockerfile in docker := {
  new Dockerfile {
    from("busybox")
    label(Map("com.example.bar" -> "baz", "com.example.bor" -> "boz"))
    label(("com.example.bar.boz", "boz"), ("com.example.bar.bar", "bar.boz"))
    label("com.example.bar.bon", "baz2")
  }
}

val check = taskKey[Unit]("Check")
check := {
  val process = Process("docker", Seq("run", "--rm", "--name", (imageName in docker).value.toString, (imageName in docker).value.toString))
  val imageToLabel = Process("docker", Seq("images", "--filter", "label=com.example.bar=boz"))
  val out = imageToLabel.!!
  if (out.toString.contains((imageName in docker).value.toString)) sys.error("Unexpected output: " + out)
}
