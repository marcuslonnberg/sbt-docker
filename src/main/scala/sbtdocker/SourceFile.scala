package sbtdocker

import sbt._

trait SourceFile {
  def filename: String

  def stage(stageDir: File): Unit
}

case class CopyFile(file: File) extends SourceFile {
  def filename = file.getName

  def stage(destination: File) = {
    if (file.isDirectory) {
      IO.copyDirectory(file, destination)
    } else {
      IO.copyFile(file, destination)
    }
  }
}
