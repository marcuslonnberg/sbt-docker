package sbtdocker

object Instructions {

  trait Instruction {
    def arguments: String

    def instructionName: String

    override def toString = s"$instructionName $arguments"

    @deprecated("Use toString instead.", "0.4.0")
    def toInstructionString = toString
  }

  trait ProductInstruction extends Instruction {
    this: Product =>
    def arguments = productIterator.mkString(" ")

    def instructionName = productPrefix.toUpperCase
  }

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

  case class From(image: String) extends ProductInstruction

  case class Maintainer(name: String) extends ProductInstruction

  object Run {
    def shell(args: String*) = RunShell(args: _*)

    def apply(args: String*) = RunExec(args: _*)

    def raw(command: String) = RunRaw(command)
  }

  /**
   * RUN instruction.
   */
  trait Run extends Instruction {
    override def instructionName = "RUN"
  }

  /**
   * RUN instruction on exec form.
   *
   * Example:
   * {{{
   * RunExec("executable", "param 1", "param2")
   * }}}
   * Will yield instruction: RUN ["executable", "param 1", "param2"]
   */
  case class RunExec(args: String*) extends Run with SeqArguments {
    override def shellFormat = false
  }

  /**
   * RUN instruction on exec form.
   *
   * Example:
   * {{{
   * RunShell("executable", "param 1", "param2")
   * }}}
   * Will yield instruction: RUN executable "param 1" param2
   */
  case class RunShell(args: String*) extends Run with SeqArguments {
    override def shellFormat = true
  }

  /**
   * Raw RUN instruction.
   *
   * Example:
   * {{{
   * RunRaw("executable param 1 param2")
   * }}}
   * Will yield instruction: RUN executable param 1 param2
   */
  case class RunRaw(command: String) extends Run {
    override def arguments = command
  }

  object Cmd {
    def shell(args: String*) = new Cmd(true, args: _*)

    def apply(args: String*) = new Cmd(false, args: _*)
  }

  /**
   * CMD instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class Cmd(shellFormat: Boolean, args: String*) extends ProductInstruction with SeqArguments

  case class Expose(ports: Int*) extends ProductInstruction {
    override def arguments = ports.mkString(" ")
  }

  case class Env(key: String, value: String) extends ProductInstruction

  case class Add(from: String, to: String) extends ProductInstruction

  case class Copy(from: String, to: String) extends ProductInstruction

  object EntryPoint {
    def shell(args: String*) = new EntryPoint(true, args: _*)

    def apply(args: String*) = new EntryPoint(false, args: _*)
  }

  /**
   * ENTRYPOINT instruction.
   * @param shellFormat true if the command should be executed in a shell
   * @param args command
   */
  case class EntryPoint(shellFormat: Boolean, args: String*) extends ProductInstruction with SeqArguments

  case class Volume(mountPoint: String) extends ProductInstruction

  case class User(username: String) extends ProductInstruction

  case class WorkDir(path: String) extends ProductInstruction

  case class OnBuild(instruction: Instruction) extends ProductInstruction {
    override def arguments = instruction.toString
  }

}
