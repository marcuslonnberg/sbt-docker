Example with Spray using package task
=====================================
This example makes use of [Spray](http://spray.io) to run a simple web server.

Build the image by running `sbt docker` in this directory.

Run the produced image with `docker run -p 8080:8080 sbtdocker/example-package-spray`.
The web server will now be accessible on port [8080](http://localhost:8080).
