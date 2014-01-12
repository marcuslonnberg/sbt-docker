Example with Spray using package task
=====================================
This example makes use of [Spray](http://spray.io) in order to run a simple web server.

Build the image by running `sbt docker` in this directory.

Run the produced image with the following command `docker run -p 8080:8080 sbt-docker/example-package-spray`
The web server will now be accessible on port [8080](http://localhost:8080).