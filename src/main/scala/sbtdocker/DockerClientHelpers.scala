package sbtdocker

import com.spotify.docker.client.DockerClient.BuildParam

object DockerClientHelpers {
  def buildParams(buildOptions: BuildOptions): Seq[BuildParam] = {
    val cacheParam = if (buildOptions.cache) None else Some(BuildParam.noCache())

    val removeParam = buildOptions.removeIntermediateContainers match {
      case BuildOptions.Remove.Always =>
        BuildParam.forceRm()
      case BuildOptions.Remove.Never =>
        BuildParam.rm(false)
      case BuildOptions.Remove.OnSuccess =>
        BuildParam.rm(true)
    }

    val pullParam = buildOptions.pullBaseImage match {
      case BuildOptions.Pull.Always =>
        Some(BuildParam.pullNewerImage())
      case BuildOptions.Pull.IfMissing =>
        None
    }

    Seq(cacheParam, Some(removeParam), pullParam).flatten
  }
}
