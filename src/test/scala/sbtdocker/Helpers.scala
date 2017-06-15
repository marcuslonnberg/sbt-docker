package sbtdocker

import java.io.File

object Helpers {
  implicit class RichFile(val asFile: File) {
    def /(component: String): File = if (component == ".") asFile else new File(asFile, component)
  }
}
