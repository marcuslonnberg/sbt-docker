package simple

import org.joda.time.DateTime

object Hello extends App {
  println("Hello AutoPackage")
  println(new DateTime().getYear().toString.take(2))
}
