name := "example-sbt-assembly"

organization := "sbtdocker"

version := "0.1.0"

enablePlugins(DockerFromFilePlugin)


dockerFromFile in docker := {
  val artifact: File = assembly.value

  new DockerFromFile {
    from(baseDirectory.value / "Dockerfile")
    stageFile(artifact, "example-sbt-assembly-assembly-0.1.0.jar")
  }
}
// Set names for the image
imageNames in docker := Seq(
  ImageName("sbtdocker/test:stable"),
  ImageName(namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
)

buildOptions in docker := BuildOptions(cache = false)
