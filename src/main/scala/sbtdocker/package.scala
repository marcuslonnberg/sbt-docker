package object sbtdocker {
  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  @deprecated("Use instruction with same name instead", "0.6.0")
  type StageFile = Instructions.StageFile
  @deprecated("Use instruction with same name instead", "0.6.0")
  val StageFile = Instructions.StageFile
}
