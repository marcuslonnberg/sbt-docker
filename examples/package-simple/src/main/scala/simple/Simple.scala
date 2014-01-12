package simple

import scala.concurrent.duration._

object Simple extends App {
  println("Application started")
  println("Sleeping...")
  Thread.sleep(2.seconds.toMillis)
  println("Application exiting")
}
