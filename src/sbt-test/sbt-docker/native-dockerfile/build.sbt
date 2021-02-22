enablePlugins(DockerPlugin)

name := "scripted-dockerfile-file"

organization := "sbtdocker"

version := "0.1.0"

// Define a Dockerfile
docker / dockerfile := NativeDockerfile(file("Dockerfile"))

// Set a custom image name
docker / imageNames := {
  val imageName = ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
  Seq(imageName, imageName.copy(tag = Some("latest")))
}

val check = taskKey[Unit]("Check")

check := {
  val names = (docker / imageNames).value
  names.foreach { imageName =>
    val process = scala.sys.process.Process("docker", Seq("run", "--rm", imageName.toString))
    val out = process.!!
    if (out.trim != "Hello World") sys.error("Unexpected output: " + out)
  }
}
