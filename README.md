sbt-docker
==========
sbt-docker is a [sbt](http://www.scala-sbt.org/) plugin which builds [Docker](http://www.docker.io/) images for your projects.

Requirements
------------
* sbt
* Docker

Setup
-----

Add sbt-docker as a dependency in `project/plugins.sbt`:
```scala
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.5.2")
```

sbt-docker is an [auto plugin](http://www.scala-sbt.org/0.13/docs/Plugins.html), this means that sbt 0.13.5 or newer is required.

Usage
-----

Start by enabling the plugin in your `build.sbt` file:
```scala
enablePlugins(DockerPlugin)
```

This sets up some settings with default values and adds the `docker` task which builds a Docker image.
The only setting that required to be defined is `dockerfile in docker`.

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
dockerfile in docker := {
  val jarFile = artifactPath.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("dockerfile/java")
    // Add all files on the classpath
    classpath.files.foreach { file =>
      add(file, "/app/")
    }
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}
```

Example with the [sbt-assembly](https://github.com/sbt/sbt-assembly) plugin:
```scala
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

To build an image use the `docker` task.
Simply run `sbt docker` from your prompt or `docker` in the sbt console.

### Pushing an image

An image that have already been built can be pushed with the `dockerPush` task.
To both build and push an image use the `dockerBuildAndPush` task.

The `imageNames in docker` key is used to determine which image names to push.

### Custom image names

Set `imageNames in docker` of type `Seq[sbtdocker.ImageName]`.

Example:
```scala
imageNames in docker := Seq(
  ImageName("organization/name:tag"),
  ImageName(
  	namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value))
)
```

### Build options

Use the key `buildOptions in docker` to set build options.

Example:
```scala
buildOptions in docker := BuildOptions(
  cache = false,
  removeIntermediateContainers = BuildOptions.Remove.Always,
  alwaysPullBaseImage = BuildOptions.Pull.Always)
```

### Auto packaging JVM applications

If you quickly just want to
If you just want to run a JVM application in an Docker image
Instead of `dockerSettings` the method `dockerSettingsAutoPackage(fromImage, exposedPorts)` can be used.
This method defines a Dockerfile automatically and uses the `package` task to try to generate an artifact.
It's intended purpose is to give a very simple way of creating Docker images for new small projects.

### Example projects

See [example projects](examples).
