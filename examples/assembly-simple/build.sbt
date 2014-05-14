import sbtdocker.BuildOptions
import sbtdocker.Plugin._
import sbtdocker.Plugin.DockerKeys._
import sbtassembly.Plugin.AssemblyKeys
import AssemblyKeys._

assemblySettings

name := "example-assembly-simple"

organization := "sbtdocker"

version := "0.1.0"


// Import default settings and a predefined Dockerfile that expects a single (fat) jar
dockerSettingsSingleJar

// Make docker depend on the assembly task, which generates a fat jar file
docker <<= (docker dependsOn assembly)

// Tell docker which jarFile to add to the container
jarFile in docker <<= (outputPath in assembly)

buildOptions in docker := BuildOptions(noCache = Some(true))
