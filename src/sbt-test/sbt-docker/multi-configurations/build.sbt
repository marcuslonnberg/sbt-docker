enablePlugins(DockerPlugin)

name := "scripted-multi-configurations"
organization := "sbtdocker"


lazy val alfa = config("alfa")
inConfig(alfa)(sbtdocker.DockerSettings.baseDockerSettings)

imageName in docker in alfa := ImageName("sbtdocker/scripted-multi-configurations-alfa")
target in docker in alfa := target.value / "docker-alfa"
dockerfile in docker in alfa := new Dockerfile {
  from("busybox")
  entryPoint("echo", "alfa")
}


lazy val bravo = config("bravo")
inConfig(bravo)(sbtdocker.DockerSettings.baseDockerSettings)

imageName in docker in bravo := ImageName("sbtdocker/scripted-multi-configurations-bravo")
target in docker in bravo := target.value / "docker-bravo"
dockerfile in docker in bravo := new Dockerfile {
  from("busybox")
  entryPoint("echo", "bravo")
}


def checkImage(imageName: ImageName, expectedOut: String) {
  val process = Process("docker", Seq("run", "--rm", imageName.name))
  val out = process.!!
  if (out.trim != expectedOut) sys.error(s"Unexpected output (${imageName.name}): $out")
}

val check = taskKey[Unit]("Check")

check := {
  checkImage((imageName in docker in alfa).value, "alfa")
  checkImage((imageName in docker in bravo).value, "bravo")
}
