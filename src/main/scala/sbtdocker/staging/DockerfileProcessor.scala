package sbtdocker.staging
import sbt._
import sbtdocker.DockerfileLike

trait DockerfileProcessor {
  def apply(dockerfile: DockerfileLike, stageDir: File): StagedDockerfile
}
