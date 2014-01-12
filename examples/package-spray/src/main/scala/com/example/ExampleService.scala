package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

class ExampleServiceActor extends Actor with ExampleService {
  def actorRefFactory = context

  def receive = runRoute(route)
}

trait ExampleService extends HttpService {
  val route =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <h1>Hello World!</h1>
              </body>
            </html>
          }
        }
      }
    }
}