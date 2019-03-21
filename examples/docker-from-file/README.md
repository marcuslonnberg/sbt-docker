Example with docker-from-file
=========================

This example makes use of the [sbt-assembly](https://github.com/sbt/sbt-assembly) plugin to build a fat JAR file.

Build a new image by running `sbt docker` in this directory.

Then run the produced image with `docker run -i sbtdocker/example-sbt-assembly:v0.1.0`.
