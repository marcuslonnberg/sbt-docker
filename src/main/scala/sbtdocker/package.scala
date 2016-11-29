package object sbtdocker {
  val Dockerfile = mutable.Dockerfile
  type Dockerfile = mutable.Dockerfile

  type ImmutableDockerfile = immutable.Dockerfile
  val ImmutableDockerfile = immutable.Dockerfile
}
