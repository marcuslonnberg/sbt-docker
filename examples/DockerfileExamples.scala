import java.io.File

import sbtdocker.Instructions._
import sbtdocker._
import staging.CopyFile

// There is both a mutable and an immutable Dockerfile.
// Both share the same API where all Dockerfile instructions have a corresponding method.

val jarFile: File = ???

// An immutable Dockerfile

immutable.Dockerfile.empty
  .from("ubuntu")
  .run("apt-get", "-y", "install", "openjdk-7-jre-headless")
  .add(jarFile, "/srv/app.jar")
  .workDir("/srv")
  .cmdRaw("java -jar app.jar")

// A mutable Dockerfile (which does the same as the immutable example)

new mutable.Dockerfile {
  from("ubuntu")
  run("apt-get", "-y", "install", "openjdk-7-jre-headless")
  add(jarFile, "/srv/app.jar")
  workDir("/srv")
  cmdRaw("java -jar app.jar")
}

// Some benefits of the mutable Dockerfile is that it is easy to conditionally include instructions
// and adding instructions given a collection.

val numbers = List(1, 1, 2, 3, 5, 8)
val earthIsRound = true

new mutable.Dockerfile {
  from("ubuntu")

  if (earthIsRound) {
    expose(80)
  }

  numbers foreach { n =>
    run("echo", n.toString)
  }
}

// A Dockerfile can also be created with a sequence of instructions

val instructions = Seq(
  From("ubuntu"),
  Run.exec(Seq("apt-get", "-y", "install", "openjdk-7-jre-headless")),
  Add(CopyFile(jarFile), "app.jar"),
  Cmd("java -jar app.jar")
)

Dockerfile(instructions)

// In order to build a Dockerfile that exists already in the filesystem use the NativeDockerfile class:

NativeDockerfile(file("subdirectory") / "Dockerfile")
