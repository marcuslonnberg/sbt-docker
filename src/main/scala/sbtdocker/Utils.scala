package sbtdocker

import sbt._

private[sbtdocker] object Utils {
  def expandPath(source: File, path: String) = {
    val pathFile = file(path)
    if (path.endsWith("/")) pathFile / source.name
    else pathFile
  }
}
