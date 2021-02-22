enablePlugins(DockerPlugin)

name := "build-arguments"

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
    arg("buildArgument1")
    arg("buildArgument2")
    arg("buildArgument3", Some("default Value3"))
    env(Map(
      "buildArgument1" -> "$buildArgument1",
      "buildArgument2" -> "$buildArgument2",
      "buildArgument3" -> "$buildArgument3"
    ))

    // Copy all files that is on the classpath
    files.foreach {
      case (source, destination) =>
        copy(source, destination)
    }
    // Copy the JAR and set the entry point
    copy(jarFile, jarTarget)
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

// Set a custom image name
docker / imageNames := {
  val imageName = ImageName(namespace = Some(organization.value), repository = name.value, tag = Some("v" + version.value))
  Seq(imageName, imageName.copy(tag = Some("latest")))
}

docker / dockerBuildArguments := Map(
  "buildArgument1" -> "value 1",
  "buildArgument2" -> "value$2"
)

val check = taskKey[Unit]("Check")

check := {
  val names = (docker / imageNames).value
  names.foreach { imageName =>
    val process = scala.sys.process.Process("docker", Seq("run", "--rm", imageName.toString))
    val out = process.!!
    if (out.trim != "value 1 value$2 default Value3") sys.error("Unexpected output: " + out)
  }
}
