name := "example-package-basic"

organization := "sbtdocker"

version := "0.1.0"

enablePlugins(DockerPlugin)

// Define a Dockerfile
docker / dockerfile := {
  val jarFile = (Compile / packageBin / sbt.Keys.`package`).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = (Compile / packageBin / mainClass).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName).mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("openjdk:8-jre")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

// Set names for the image
docker / imageNames := Seq(
  ImageName("sbtdocker/basic:stable"),
  ImageName(namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
)
