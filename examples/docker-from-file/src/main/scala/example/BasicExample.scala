package example

import scala.concurrent.duration._

object BasicExample extends App {
  println("Application started")
  println("Sleeping...")
  Thread.sleep(2.seconds.toMillis)
  println("Application exiting")
}
