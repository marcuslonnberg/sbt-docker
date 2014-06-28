package sbtdocker

import org.scalatest.{FunSuite, Matchers}

class ImageNameSuite extends FunSuite with Matchers {
  test("name") {
    ImageName("test").toString shouldEqual "test"

    ImageName(namespace = Some("sbtdocker"), repository = "test").toString shouldEqual "sbtdocker/test"

    ImageName(repository = "test", tag = Some("v2")).toString shouldEqual "test:v2"

    ImageName(registry = Some("registry.example.com"), repository = "test").toString shouldEqual "registry.example.com/test"

    ImageName(
      registry = Some("registry.example.com"),
      namespace = Some("sbtdocker"),
      repository = "test",
      tag = Some("v2")).toString shouldEqual "registry.example.com/sbtdocker/test:v2"
  }
}
