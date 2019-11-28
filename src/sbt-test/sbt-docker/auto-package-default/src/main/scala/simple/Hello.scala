package simple

object Hello extends App {
  println(System.getProperty("my.system.property"))
  args.foreach(println)
}
