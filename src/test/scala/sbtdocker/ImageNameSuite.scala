package sbtdocker

import org.scalatest.{FunSuite, Matchers}

class ImageNameSuite extends FunSuite with Matchers {
  test("name") {
    ImageName("test").name shouldEqual "test"

    ImageName(namespace = Some("sbtdocker"), repository = "test").name shouldEqual "sbtdocker/test"

    ImageName(repository = "test", tag = Some("v2")).name shouldEqual "test:v2"

    ImageName(registry = Some("registry.example.com"), repository = "test").name shouldEqual "registry.example.com/test"

    ImageName(
      registry = Some("registry.example.com"),
      namespace = Some("sbtdocker"),
      repository = "test",
      tag = Some("v2")).name shouldEqual "registry.example.com/sbtdocker/test:v2"
  }
}
