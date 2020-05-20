import sbtdocker.DockerfileInstruction

enablePlugins(DockerPlugin)

name := "build-args-sample"

organization := "sbtdocker"

version := "0.1.0"

buildArgs in docker := Map("person_to_greet" -> "world")

// Define a Dockerfile
dockerfile in docker := {
  new Dockerfile {
    from("busybox")
    addInstruction(new DockerfileInstruction {
      override def instructionName: String = "ARG"
      override def arguments: String = "person_to_greet"
    })
    envRaw("PERSON_TO_GREET=$person_to_greet")
    cmdRaw("""echo "Hello $PERSON_TO_GREET!"""")
  }
}

val check = taskKey[Unit]("Check")

check := {
  val names = (imageNames in docker).value
  names.foreach { imageName =>
    val process = Process("docker", Seq("run", "--rm", imageName.toString))
    val out = process.!!
    if (out.trim != "Hello world!") sys.error("Unexpected output: " + out)
  }
}
