enablePlugins(DockerPlugin)

name := "scripted-simple"

organization := "sbtdocker"

version := "0.1.0"

// Define a Dockerfile
dockerfile in docker := {
  val jarFile = Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse {
    sys.error("Expected exactly one main class")
  }
  val jarTarget = s"/app/${jarFile.getName}"
  // Add all files on the classpath
  val files = classpath.files.map(file => file -> s"/app/${file.getName}").toMap
  // Make a colon separated classpath with the JAR file
  val classpathString = files.values.mkString(":") + ":" + jarTarget
  new Dockerfile {
    from("java")
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
dockerImageNames in docker := {
  val imageName = ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  )
  Seq(imageName, imageName.copy(tag = Some("latest")))
}

val check = taskKey[Unit]("Check")

check := {
  val names = (dockerImageNames in docker).value
  names.foreach { imageName =>
    val process = Process("docker", Seq("run", "--rm", imageName.toString))
    val out = process.!!
    if (out.trim != "Hello World") sys.error("Unexpected output: " + out)
  }
}
