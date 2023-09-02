enablePlugins(DockerPlugin)

name := "scripted-rmi"

organization := "sbtdocker"

version := "0.1.0"

// Define a Dockerfile
docker / dockerfile := {
  val jarFile = (Compile / packageBin / Keys.`package`).value
  val classpath = (Compile / managedClasspath).value
  val mainclass = (Compile / packageBin / mainClass).value.getOrElse {
    sys.error("Expected exactly one main class")
  }
  val jarTarget = s"/app/${jarFile.getName}"
  // Add all files on the classpath
  val files = classpath.files.map(file => file -> s"/app/${file.getName}").toMap
  // Make a colon separated classpath with the JAR file
  val classpathString = files.values.mkString(":") + ":" + jarTarget
  new Dockerfile {
    from("openjdk:8-jre")
    // Add all files that is on the classpath
    files.foreach {
      case (source, destination) =>
        add(source, destination)
    }
    // Add the JAR and set the entry point
    add(jarFile, jarTarget)
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

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
