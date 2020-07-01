enablePlugins(DockerPlugin)

name := "scripted-multi-configurations"
organization := "sbtdocker"


lazy val Alfa = config("Alfa")
inConfig(Alfa)(sbtdocker.DockerSettings.baseDockerSettings)

imageNames in docker in Alfa := Seq(
  ImageName("sbtdocker/scripted-multi-configurations-alfa")
)
target in docker in Alfa := target.value / "docker-alfa"
dockerfile in docker in Alfa := new Dockerfile {
  from("busybox")
  entryPoint("echo", "alfa")
}


lazy val Bravo = config("Bravo")
inConfig(Bravo)(sbtdocker.DockerSettings.baseDockerSettings)

imageNames in docker in Bravo := Seq(
  ImageName("sbtdocker/scripted-multi-configurations-bravo")
)
target in docker in Bravo := target.value / "docker-bravo"
dockerfile in docker in Bravo := new Dockerfile {
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
  checkImage((imageNames in docker in Alfa).value.head, "alfa")
  checkImage((imageNames in docker in Bravo).value.head, "bravo")
}
