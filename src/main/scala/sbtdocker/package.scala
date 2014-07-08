package object sbtdocker {
  type Instruction = Instructions.Instruction

  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile
}
