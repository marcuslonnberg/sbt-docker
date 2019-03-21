enablePlugins(DockerFromFilePlugin)

name := "docker-from-file"

organization := "sbtdocker"

version := "0.1.0"

scalaVersion := "2.11.5"

libraryDependencies += "joda-time" % "joda-time" % "2.7"

dockerFromFile in docker := {
  new DockerFromFile {
    from(baseDirectory.value / "Dockerfile")
    stageFile(target.value / "scala-2.11/docker-from-file_2.11-0.1.0.jar",
      "docker-from-file_2.11-0.1.0.jar")
    val appPath = "/app"
    val libsPath = s"$appPath/libs/"
    val classpath = Keys.managedClasspath.in(Compile).value
    // add scala jar
    val libPaths = classpath.files.filter(_.getName.contains("scala-library")).map { libFile =>
      val toPath = file(libsPath) / libFile.name
      stageFile(libFile, libFile.name)
    }
  }
}

imageNames in docker := Seq(
  ImageName("sbtdocker/basic:stable"),
  ImageName(namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
)

val check = taskKey[Unit]("Check")

check := {
  val process = Process("docker", Seq("run", "--rm", "sbtdocker/docker-from-file:v0.1.0"))
  val out = process.!!
  if (out.trim != "Hello Docker from text file") sys.error("Unexpected output: " + out)
}
