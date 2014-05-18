import sbtdocker._
import DockerKeys._
import AssemblyKeys._

assemblySettings

name := "example-sbt-assembly"

organization := "sbtdocker"

version := "0.1.0"


// Import default settings and a predefined Dockerfile that expects a single (fat) jar
dockerSettings

// Make docker depend on the assembly task, which generates a fat jar file
docker <<= (docker dependsOn assembly)

dockerfile in docker <<= (outputPath in assembly) map { artifact =>
  val appDirPath = "/app"
  val artifactTargetPath = s"$appDirPath/${artifact.name}"
  new Dockerfile {
    from("dockerfile/java")
    add(artifact, artifactTargetPath)
    workDir(appDirPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

buildOptions in docker := BuildOptions(noCache = Some(true))
