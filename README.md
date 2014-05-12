sbt-docker
==========
sbt-docker is a [sbt](http://www.scala-sbt.org/) plugin which creates [Docker](http://www.docker.io/) images with your artifacts.

Requirements
------------
* sbt
* Docker

Setup
-----

Published release will come soon.

To use the latest snapshot add sbt-docker as a dependency in `project/docker.sbt`:
```scala
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.1.0-SNAPSHOT")
```

Usage
-----

Start by adding the following to your build file:
```scala
import DockerKeys._

dockerSettings
```

Then make the `docker` task depend on the task that produces your artifacts and
define how the docker image should be built with them at key `dockerfile in docker`.

There are several ways of producing artifacts, this example makes use of the `sbt-assembly` plugin to produce an artifact:
```scala
assemblySettings

// Make docker depend on the assembly task, which generates a fat jar file
docker <<= (docker dependsOn assembly)

// Make it clear which JAR file we should add to our container
jarFile in docker <<= (outputPath in assembly)

// Define a Dockerfile with our artifact
dockerfile in docker <<= (stageDir in docker, jarFile in docker) map {
  case (stageDir, jarFile) =>
    implicit val stageDirImplicit = stageDir
    val jarTarget = s"/app/${jarFile.getName}"
    new Dockerfile {
      from("dockerfile/java")
      add(jarFile, jarTarget)
      entryPoint("java", "-jar", jarTarget)
    }
}
```

To build an image simply run `sbt docker`.

See [example projects](examples).
