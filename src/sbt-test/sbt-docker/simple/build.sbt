import sbtdocker._
import sbtdocker.Plugin.DockerKeys._

name := "scripted-simple"

organization := "sbtdocker"

version := "0.1.0"

dockerSettings

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package` in(Compile, packageBin))

// Define a Dockerfile
dockerfile in docker := {
  val jarFile = artifactPath.in(Compile, packageBin).value
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
    from("dockerfile/java")
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
imageName in docker := {
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
}

val check = taskKey[Unit]("Check")

check := {
  val process = Process("docker", Seq("run", (imageName in docker).value.toString))
  val out = process.!!
  if (out.trim != "Hello World") sys.error("Unexpected output: " + out)
}
