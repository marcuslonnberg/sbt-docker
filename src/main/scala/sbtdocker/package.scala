package object sbtdocker {
  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  @deprecated("Use sbtdocker.Instructions.StageFiles", "0.6.0")
  type StageFile = Instructions.StageFiles
  @deprecated("Use sbtdocker.Instructions.StageFiles", "0.6.0")
  val StageFile = Instructions.StageFiles
}
