import sbt.Keys.{artifactPath, libraryDependencies, mainClass, managedClasspath, name, organization, packageBin, resolvers, version}

name := "example-package-spray"

organization := "sbtdocker"

version := "0.1.0"

resolvers += "spray repo" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "io.spray" % "spray-can" % "1.2.0",
  "io.spray" % "spray-routing" % "1.2.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3")

enablePlugins(DockerPlugin)

// Define a Dockerfile
docker / dockerfile := {
  val jarFile = (Compile / packageBin / Keys.`package`).value
  val classpath = (Compile / managedClasspath).value
  val mainclass = (Compile / packageBin / mainClass).value.get
  val libs = "/app/libs"
  val jarTarget = "/app/" + jarFile.name

  new Dockerfile {
    // Use a base image that contain Java
    from("openjdk:8-jre")
    // Expose port 8080
    expose(8080)

    // Copy all dependencies to 'libs' in the staging directory
    classpath.files.foreach { depFile =>
      val target = file(libs) / depFile.name
      stageFile(depFile, target)
    }
    // Add the libs dir from the
    addRaw(libs, libs)

    // Add the generated jar file
    add(jarFile, jarTarget)
    // The classpath is the 'libs' dir and the produced jar file
    val classpathString = s"$libs/*:$jarTarget"
    // Set the entry point to start the application using the main class
    cmd("java", "-cp", classpathString, mainclass)
  }
}
