package object sbtdocker {
  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  @deprecated("Use sbtdocker.Instructions.StageFile", "0.6.0")
  type StageFile = Instructions.StageFile
  @deprecated("Use sbtdocker.Instructions.StageFile", "0.6.0")
  val StageFile = Instructions.StageFile
}
