package sbtdocker

import org.scalatest.{FlatSpec, Matchers}
import sbtdocker.Instructions._


class LabelSpec extends FlatSpec with Matchers {
  "A Label" should "parse 'n = v'" in {
    val label = Label(Map("n" -> "v"))
    label shouldEqual Label("n=v")
  }

  it should "parse 'com.example.bar=baz com.example.bor=boz'" in {
    val label = Label(Map("com.example.bar" -> "baz", "com.example.bor" -> "boz"))
    label shouldEqual Label("com.example.bar=baz com.example.bor=boz")
  }

}
