package sbtdocker

trait Instruction {
  this: Product =>
  def arguments = productIterator.mkString(" ")

  def instructionName = productPrefix.toUpperCase

  override def toString = s"$instructionName $arguments"
}

object Instructions {

  private def escapeQuotationMarks(str: String) = str.replace("\"", "\\\"")

  private def escapeWhitespaces(str: String) = str.replace("\n", "\\n").replace("\t", "\\t")

  trait SeqArguments {
    this: Instruction =>
    def args: Seq[String]

    def shellFormat: Boolean

    private def execArguments = args.map(escapeQuotationMarks).map(escapeWhitespaces).mkString("[\"", "\", \"", "\"]")

    private def wrapIfWhitespaces(argument: String) = {
      if (argument.exists(_.isWhitespace)) {
        '"' + argument + '"'
      } else {
        argument
      }
    }

    private def shellArguments = args.map(escapeQuotationMarks).map(escapeWhitespaces).map(wrapIfWhitespaces).mkString(" ")

    override def arguments = if (shellFormat) shellArguments else execArguments
  }

  case class From(image: String) extends Instruction

  case class Maintainer(name: String) extends Instruction

  object Run {
    def shell(args: String*) = new Run(true, args: _*)

    def apply(args: String*) = new Run(false, args: _*)
  }

  /**
   * RUN instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class Run(shellFormat: Boolean, args: String*) extends Instruction with SeqArguments

  object Cmd {
    def shell(args: String*) = new Cmd(true, args: _*)

    def apply(args: String*) = new Cmd(false, args: _*)
  }

  /**
   * CMD instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class Cmd(shellFormat: Boolean, args: String*) extends Instruction with SeqArguments

  case class Expose(ports: Int*) extends Instruction {
    override def arguments = ports.mkString(" ")
  }

  case class Env(key: String, value: String) extends Instruction

  case class Add(from: String, to: String) extends Instruction

  case class Copy(from: String, to: String) extends Instruction

  object EntryPoint {
    def shell(args: String*) = new EntryPoint(true, args: _*)

    def apply(args: String*) = new EntryPoint(false, args: _*)
  }

  /**
   * ENTRYPOINT instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class EntryPoint(shellFormat: Boolean, args: String*) extends Instruction with SeqArguments

  case class Volume(mountPoint: String) extends Instruction

  case class User(username: String) extends Instruction

  case class WorkDir(path: String) extends Instruction

  case class OnBuild(instruction: Instruction) extends Instruction {
    override def arguments = instruction.toString
  }

}
