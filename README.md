sbt-docker
==========
sbt-docker is a plugin for [sbt](http://www.scala-sbt.org/) which lets you create [Docker](http://www.docker.io/) containers for your projects without leaving sbt.

Requirements
------------
* sbt
* Docker

Setup
-----
Specify sbt-docker as a dependency in `project/project/Build.scala`:
```scala
import sbt._

object Plugins extends Build {
	lazy val root = Project("root", file(".")) dependsOn docker
	lazy val docker = uri("git://github.com/marcuslonnberg/sbt-docker.git")
}
```

Usage
-----
See [example projects](examples).

