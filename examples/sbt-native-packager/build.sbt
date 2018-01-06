name := "example-sbt-native-packager"

organization := "sbtdocker"

version := "0.1.0"

// Need to use full name to DockerPlugin, since sbt-native-packager uses the same name for its Docker plugin
enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

dockerfile in docker := {
  val appDir = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("openjdk:8-jre")
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    copy(appDir, targetDir)
  }
}

buildOptions in docker := BuildOptions(cache = false)
