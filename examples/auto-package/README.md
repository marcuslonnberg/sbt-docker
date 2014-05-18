Example with auto packaging
===========================

This example uses `dockerSettingsAutoPackage()` which automatically defines a Dockerfile that adds a JAR of the project
and all of the libraries on the classpath.

Build the image by running `sbt docker` in this directory.

Then run the produced image with `docker run -i sbtdocker/example-auto-package`.
