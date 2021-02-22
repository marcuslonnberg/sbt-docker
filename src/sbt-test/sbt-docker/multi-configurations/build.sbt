enablePlugins(DockerPlugin)

name := "scripted-multi-configurations"
organization := "sbtdocker"


lazy val Alfa = config("Alfa")
inConfig(Alfa)(sbtdocker.DockerSettings.baseDockerSettings)

Alfa / docker / imageNames := Seq(
  ImageName("sbtdocker/scripted-multi-configurations-alfa")
)
Alfa / docker / target := target.value / "docker-alfa"
Alfa / docker / dockerfile := new Dockerfile {
  from("busybox")
  entryPoint("echo", "alfa")
}


lazy val Bravo = config("Bravo")
inConfig(Bravo)(sbtdocker.DockerSettings.baseDockerSettings)

Bravo / docker / imageNames := Seq(
  ImageName("sbtdocker/scripted-multi-configurations-bravo")
)
Bravo / docker / target := target.value / "docker-bravo"
Bravo / docker / dockerfile := new Dockerfile {
  from("busybox")
  entryPoint("echo", "bravo")
}


def checkImage(imageName: ImageName, expectedOut: String) {
  val process = scala.sys.process.Process("docker", Seq("run", "--rm", imageName.toString))
  val out = process.!!
  if (out.trim != expectedOut) sys.error(s"Unexpected output (${imageName.toString}): $out")
}

val check = taskKey[Unit]("Check")

check := {
  checkImage((Alfa / docker / imageNames).value.head, "alfa")
  checkImage((Bravo / docker / imageNames).value.head, "bravo")
}
