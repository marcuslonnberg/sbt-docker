package sbtdocker

import sbt._
import scala.sys.process.{Process, ProcessLogger}
import scala.sys.error
import sbtdocker.Dockerfile.CopyFile

object DockerBuilder {
  def apply(dockerFile: Dockerfile, imageName: String, stageDir: File, log: Logger) = {
    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    IO.delete(stageDir)

    IO.write(stageDir / "Dockerfile", dockerFile.toString)
    copyFiles(dockerFile.pathsToCopy, stageDir, log)

    log.info(s"Starting to build Dockerfile")
    val processLog = ProcessLogger({
      line =>
        log.info(line)
    })
    val dockerBin = "docker"
    Process(Seq(dockerBin, "build", "-t", imageName, "."), stageDir) ! processLog match {
      case 0 => log.info(s"Successfully built docker image: $imageName")
      case n => error(s"Error when building Dockerfile, exit code: $n")
    }
  }

  def copyFiles(pathsToCopy: Seq[CopyFile], stageDir: File, log: Logger) = {
    for (CopyFile(source, targetRelative) <- pathsToCopy) {
      val target = stageDir / targetRelative.getPath
      log.debug(s"Copying '${source.toPath}' to '${target.getPath}'")

      if (target.exists())
        error( s"""Path "${target.getPath}" already exists in stage directory""")

      if (source.isFile)
        IO.copyFile(source, target, preserveLastModified = true)
      else if (source.isDirectory)
        IO.copyDirectory(source, target, overwrite = false, preserveLastModified = true)
    }
  }
}