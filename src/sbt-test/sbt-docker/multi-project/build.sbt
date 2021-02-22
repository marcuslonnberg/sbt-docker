enablePlugins(DockerPlugin)

organization in ThisBuild := "sbtdocker"

lazy val alfa = project.in(file("alfa"))
  .settings(name := "scripted-multi-project-alfa")
  .enablePlugins(DockerPlugin)
  .settings(docker / dockerfile := new Dockerfile {
    from("busybox")
    entryPoint("echo", "alfa")
})

lazy val bravo = project.in(file("bravo"))
  .settings(name := "scripted-multi-project-bravo")
  .enablePlugins(DockerPlugin)
  .settings(docker / dockerfile := new Dockerfile {
    from("busybox")
    entryPoint("echo", "bravo")
})

def checkImage(imageName: ImageName, expectedOut: String) {
  val process = scala.sys.process.Process("docker", Seq("run", "--rm", imageName.toString))
  val out = process.!!
  if (out.trim != expectedOut) sys.error(s"Unexpected output ($imageName): $out")
}

val check = taskKey[Unit]("Check")

check := {
  checkImage((alfa / docker / imageNames).value.head, "alfa")
  checkImage((bravo / docker / imageNames).value.head, "bravo")
}
