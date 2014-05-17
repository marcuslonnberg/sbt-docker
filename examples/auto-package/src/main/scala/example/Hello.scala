package example

object Hello extends App {
  println("Hello World")
  sys.props.get("java.runtime.version") foreach { version =>
    println(s"Java runtime version: $version")
  }
  println("Goodbye")
}
