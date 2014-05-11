import sbtdocker.Dockerfile
import sbtdocker.Plugin._
import sbtdocker.Plugin.DockerKeys._
import sbtassembly.Plugin.AssemblyKeys
import AssemblyKeys._

assemblySettings

name := "example-assembly-simple"

organization := "sbtdocker"

version := "0.1.0"


dockerSettingsBasic

// Make docker depend on the assembly task, which generates a fat jar file
docker <<= docker.dependsOn (assembly)

// Tell docker at which path the jar file will be created
jarFile in docker <<= (outputPath in assembly)
