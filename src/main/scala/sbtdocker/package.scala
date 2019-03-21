package object sbtdocker {
  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  type ImmutableDockerfile = immutable.Dockerfile
  val ImmutableDockerfile = immutable.Dockerfile

  val DockerFromFile = mutable.DockerFromFile
  type DockerFromFile = mutable.DockerFromFile

  type ImmutableDockerFromFile = immutable.DockerFromFile
  val ImmutableDockerFromFile = immutable.DockerFromFile

  @deprecated("Use sbtdocker.Instructions.StageFiles", "1.0.0")
  type StageFile = Instructions.StageFiles
  @deprecated("Use sbtdocker.Instructions.StageFiles", "1.0.0")
  val StageFile = Instructions.StageFiles
}
