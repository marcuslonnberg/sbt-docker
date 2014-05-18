Examples
========

There are 4 different examples:

* [auto-package](auto-package) - Automatically defines a Dockerfile
* [package-basic](package-basic) - Uses the `package` task to generate an artifact
* [package-spray](package-spray) - Similar to package-basic but with library dependencies and a more advanced example
* [sbt-assembly](sbt-assembly) - The sbt-assembly plugin is used to generate one fat JAR

Note that the examples depend on the source code of the plugin, therefore they lack the `project/docker.sbt` file.

[DockerfileExamples](DockerfileExamples.scala) shows different ways of defining a Dockerfile.
