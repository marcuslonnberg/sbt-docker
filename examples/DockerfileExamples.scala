import sbtdocker.Dockerfile
import sbtdocker.Instructions._

// Sequence of instructions

val instructions = Seq(
  From("ubuntu"),
  Run("apt-get", "-y", "install", "openjdk-7-jre-headless"),
  Add("app.jar", "app.jar"),
  Cmd("java", "-jar", "app.jar")
)

val dockerfile1 = Dockerfile(instructions)

// Mutable Dockerfile

val dockerfile2 = Dockerfile()
dockerfile2.from("ubuntu")
dockerfile2.run("apt-get", "-y", "install", "openjdk-7-jre-headless")
dockerfile2.add("app.jar", "app.jar")
// Conditionally include instructions
if (true) {
  dockerfile2.cmd("java", "-jar", "app.jar")
}


val dockerfile3 = new Dockerfile {
  from("ubuntu")
  run("apt-get", "-y", "install", "openjdk-7-jre-headless")
  add("app.jar", "app.jar")
  // Conditionally include instructions
  if (true) {
    cmd("java", "-jar", "app.jar")
  }
}

// They are all the same
dockerfile1 == dockerfile2 && dockerfile2 == dockerfile3
