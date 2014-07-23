sbt-docker
==========
sbt-docker is a [sbt](http://www.scala-sbt.org/) plugin which creates [Docker](http://www.docker.io/) images with your artifacts.

Requirements
------------
* sbt
* Docker

Setup
-----

Latest version is `0.3.0`.

Add sbt-docker as a dependency in `project/docker.sbt`:
```scala
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.3.0")
```

To use the latest snapshot set the following in `project/docker.sbt`:
```scala
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.3.1-SNAPSHOT")
```

Usage
-----

Start by adding the following to your `build.sbt` file:
```scala
import DockerKeys._

dockerSettings

// add your sbt-docker settings here
```

This sets up some settings with default values and adds the `docker` task which builds the Docker image.
The only setting that is left for you to define is `dockerfile in docker`.

### Artifacts

Typically you rely on some sbt task to generate one or several artifacts that you want inside your Docker image.
For example the `package` task could be used to generate JAR files, or tasks from plugins such as `sbt-assembly`.
In those cases you may want to set the `docker` task to depend on those other tasks.
So that the artifacts are always up to date when building a Docker image.

Here is how to make the docker task depend on the sbt `package` task:
```scala
docker <<= docker.dependsOn(Keys.`package`.in(Compile, packageBin))
```

### Defining a Dockerfile

In order to produce a Docker image a Dockerfile must be defined.
It should be defined at the `dockerfile in docker` key.
There is a mutable and an immutable Dockerfile class available, both provides a DSL which resembles the plain text [Dockerfile](https://docs.docker.com/reference/builder/) format.
The mutable class is default and is used in the examples below.

Example with the sbt `package` task.
```scala
import DockerKeys._
import sbtdocker.mutable.Dockerfile

dockerfile in docker <<= (artifactPath.in(Compile, packageBin), managedClasspath in Compile, mainClass.in(Compile, packageBin)) map {
  case (jarFile, classpath, Some(mainClass)) =>
    new Dockerfile {
      // Base image
      from("dockerfile/java")
      // Add all files on the classpath
      val files = classpath.files.map { file =>
        val target = "/app/" + file.getName
        add(file, target)
        target
      }
      // Add the generated JAR file
      val jarTarget = s"/app/${jarFile.getName}"
      add(jarFile, jarTarget)
	  // Make a colon separated classpath with the JAR file
      val classpathString = files.mkString(":") + ":" + jarTarget
      // On launch run Java with the classpath and the found main class
      entryPoint("java", "-cp", classpathString, mainClass)
    }
  case (_, _, None) =>
    sys.error("Expected exactly one main class")
}
```

Example with the [sbt-assembly](https://github.com/sbt/sbt-assembly) plugin:
```scala
import AssemblyKeys._
import DockerKeys._
import sbtdocker.mutable.Dockerfile

dockerSettings

assemblySettings

// Make the docker task depend on the assembly task, which generates a fat JAR file
docker <<= (docker dependsOn assembly)

dockerfile in docker := {
  val artifact = (outputPath in assembly).value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("dockerfile/java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}
```

Have a look at [DockerfileExamples](examples/DockerfileExamples.scala) for different ways of defining a Dockerfile.

### Building an image

Simply run `sbt docker` from your prompt or `docker` in the sbt console.

### Custom image name

Set `imageName in docker` of type `sbtdocker.ImageName`.

Example:
```scala
import DockerKeys._
import sbtdocker.ImageName

imageName in docker := {
  ImageName(
  	namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
}
```

### Build options

Use the key `buildOptions in docker` to set build options.
The expected type is `sbtdocker.BuildOptions` which have flags to disable caching and removal of intermediate
containers.

Example:
```scala
import DockerKeys._
import sbtdocker.BuildOptions

buildOptions in docker := BuildOptions(noCache = Some(true))
```

### Auto packaging

Instead of `dockerSettings` the method `dockerSettingsAutoPackage(fromImage, exposePorts)` can be used.
This method defines a dockerfile automatically and uses the `package` task to try to generate an artifact.
It's intended purpose is to give a very simple way of creating Docker images for new small projects.

### Example projects

See [example projects](examples).
