package sbtdocker.staging

import java.nio.file.{Files, StandardCopyOption}

import sbt._

trait SourceFile {
  def filename: String

  def stage(stageDir: File): Unit
}

case class CopyFile(file: File) extends SourceFile {
  def filename: String = file.getName

  def stage(destination: File): Unit = {
    val paths = (PathFinder(file) ** AllPassFilter) pair Path.rebase(file, destination)
    paths.foreach((copy _).tupled)
  }

  private def copy(sourceDir: File, destinationDir: File): Unit = {
    if (!destinationDir.getParentFile.exists()) {
      sourceDir.getParentFile match {
        case null =>
          destinationDir.getParentFile.mkdirs()
        case sourceDirParent =>
          copy(sourceDirParent, destinationDir.getParentFile)
      }
    }

    Files.copy(
      sourceDir.toPath,
      destinationDir.toPath,
      StandardCopyOption.COPY_ATTRIBUTES,
      StandardCopyOption.REPLACE_EXISTING
    )
  }
}
