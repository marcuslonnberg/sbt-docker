package sbtdocker

import org.scalatest.{FlatSpec, Matchers}
import sbtdocker.Instructions.Run

class InstructionsSpec extends FlatSpec with Matchers {
  "Run" should "handle exec form" in {
    Run.exec(Seq("echo", "123", "\"ö'\\")).arguments shouldEqual "[\"echo\", \"123\", \"\\\"\\u00F6'\\\\\"]"
  }
}
