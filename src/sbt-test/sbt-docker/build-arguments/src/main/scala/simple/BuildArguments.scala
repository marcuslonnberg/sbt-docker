package simple

object BuildArguments extends App {
  println(s"${sys.env("buildArgument1")} ${sys.env("buildArgument2")} ${sys.env("buildArgument3")}")
}
