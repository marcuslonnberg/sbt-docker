package object sbtdocker {
  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  type ImmutableDockerfile = immutable.Dockerfile
  val ImmutableDockerfile = immutable.Dockerfile

  @deprecated("Use sbtdocker.Instructions.StageFiles", "0.6.0")
  type StageFile = Instructions.StageFiles
  @deprecated("Use sbtdocker.Instructions.StageFiles", "0.6.0")
  val StageFile = Instructions.StageFiles
}
