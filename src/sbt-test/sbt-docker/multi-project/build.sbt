enablePlugins(DockerPlugin)

organization in ThisBuild := "sbtdocker"

lazy val alfa = project.in(file("alfa"))
  .settings(name := "scripted-multi-project-alfa")
  .enablePlugins(DockerPlugin)
  .settings(dockerfile in docker := new Dockerfile {
    from("busybox")
    entryPoint("echo", "alfa")
  })

lazy val bravo = project.in(file("bravo"))
  .settings(name := "scripted-multi-project-bravo")
  .enablePlugins(DockerPlugin)
  .settings(dockerfile in docker := new Dockerfile {
    from("busybox")
    entryPoint("echo", "bravo")
  })

def checkImage(imageName: ImageName, expectedOut: String) {
  val process = Process("docker", Seq("run", "--rm", imageName.toString))
  val out = process.!!
  if (out.trim != expectedOut) sys.error(s"Unexpected output (${imageName.toString}): $out")
}

val check = taskKey[Unit]("Check")

check := {
  checkImage((dockerImageNames in docker in alfa).value.head, "alfa")
  checkImage((dockerImageNames in docker in bravo).value.head, "bravo")
}
