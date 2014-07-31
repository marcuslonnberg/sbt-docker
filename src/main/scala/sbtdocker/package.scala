package object sbtdocker {
  type Instruction = Instructions.Instruction

  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  @deprecated("Renamed to StageFile.", "0.4.0")
  val CopyPath = StageFile
  @deprecated("Renamed to StageFile.", "0.4.0")
  type CopyPath = StageFile
}
