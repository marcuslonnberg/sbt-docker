package com.example

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object WebApp extends App {
  implicit val system = ActorSystem()

  val handler = system.actorOf(Props[ExampleServiceActor], name = "example-service")

  IO(Http) ! Http.Bind(handler, interface = "0.0.0.0", port = 8080)
}