name := "example-sbt-assembly"

organization := "sbtdocker"

version := "0.1.0"

enablePlugins(DockerPlugin)

docker / dockerfile := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

docker / buildOptions := BuildOptions(cache = false)
