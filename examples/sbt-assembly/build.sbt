name := "example-sbt-assembly"

organization := "sbtdocker"

version := "0.1.0"

enablePlugins(DockerPlugin)

// Make docker depend on the assembly task, which generates a fat jar file
docker <<= (docker dependsOn assembly)

dockerfile in docker := {
  val jarFile = (outputPath in assembly).value
  val appDirPath = "/app"
  val jarTargetPath = s"$appDirPath/${jarFile.name}"
  
  new Dockerfile {
    from("java")
    add(jarFile, jarTargetPath)
    workDir(appDirPath)
    entryPoint("java", "-jar", jarTargetPath)
  }
}

buildOptions in docker := BuildOptions(cache = false)
