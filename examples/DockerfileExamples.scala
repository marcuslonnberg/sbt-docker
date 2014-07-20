import java.io.File

import sbtdocker.Instructions._
import sbtdocker._

// There is both a mutable and an immutable Dockerfile.
// Both share the same API where all Dockerfile instructions have a corresponding method and it keeps track of files
// that should be copied to the staging directory.

val jarFile: File = ???

// An immutable Dockerfile

immutable.Dockerfile.empty
  .from("ubuntu")
  .run("apt-get", "-y", "install", "openjdk-7-jre-headless")
  .add(jarFile, "/srv/app.jar")
  .workDir("/srv")
  .cmd("java", "-jar", "app.jar")

// A mutable Dockerfile (which does the same as the immutable example)

new mutable.Dockerfile {
  from("ubuntu")
  run("apt-get", "-y", "install", "openjdk-7-jre-headless")
  add(jarFile, "/srv/app.jar")
  workDir("/srv")
  cmd("java", "-jar", "app.jar")
}

// Benefits of the mutable Dockerfile is that it is easy to conditionally include instructions and adding repeated
// instructions with values from a collection.

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

// A Dockerfile can also be created with a sequence of instructions and a sequence of files that should exist in the
// stage dir.

val instructions = Seq(
  From("ubuntu"),
  Run("apt-get", "-y", "install", "openjdk-7-jre-headless"),
  Add("app.jar", "app.jar"),
  Cmd("java", "-jar", "app.jar")
)

val stagedFiles = Seq(
  CopyPath(new File("/path/to/app.jar"), new File("app.jar"))
)

Dockerfile(instructions, stagedFiles)
