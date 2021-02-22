import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

enablePlugins(DockerPlugin)

name := "scripted-file-flags"

organization := "sbtdocker"

version := "0.1.0"

docker / dockerfile := {
  new Dockerfile {
    from("busybox")
    copy(file("files"), "/files")
    cmdRaw("ls -l /files")
  }
}

val filePermissions = Map(
  "dir-executable" -> "dr-xr-xr-x",
  "dir-readable" -> "dr--r--r--",
  "dir-writable" -> "drw-rw-rw-",
  "executable" -> "-r-xr-xr-x",
  "readable" -> "-r--r--r--",
  "writable" -> "-rw-rw-rw-"
)

val createSourceFiles = taskKey[Unit]("Create source files")
createSourceFiles := {
  val sourceDir = file("files")
  sourceDir.mkdir()

  filePermissions.foreach {
    case (filename, permissionsString) =>
      val file  = sourceDir / filename
      if (permissionsString.startsWith("d")) {
        file.mkdir()
      } else {
        file.createNewFile()
      }

      val permissionsSet = PosixFilePermissions.fromString(permissionsString.tail)
      Files.setPosixFilePermissions(file.toPath, permissionsSet)
  }
}

val check = taskKey[Unit]("Check")
check := {
  val name = (docker / imageNames).value.head
  val process = scala.sys.process.Process("docker", Seq("run", "--rm", name.toString))
  val out = process.!!

  val lines = out.split('\n').drop(1)

  val result = lines.map {line =>
    val split = line.split(" ")
    split.last -> split.head
  }.toMap

  if (result != filePermissions) {
    sys.error(
      s"""Unexpected output: $out
         |Parsed: $result""".stripMargin)
  }
}
