package sbtdocker

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ImageNameSpec extends AnyFlatSpec with Matchers {
  "A ImageName" should "parse 'registry.tld:5000/namespace/repository:tag'" in {
    val name = ImageName("registry.tld:5000/namespace/repository:tag")
    name shouldEqual ImageName(
      registry = Some("registry.tld:5000"),
      namespace = Some("namespace"),
      repository = "repository",
      tag = Some("tag")
    )
  }

  it should "parse 'registry.tld/repository:tag'" in {
    val name = ImageName("registry.tld/repository:tag")
    name shouldEqual ImageName(registry = Some("registry.tld"), namespace = None, repository = "repository", tag = Some("tag"))
  }

  it should "parse 'registry:5000/repository:tag'" in {
    val name = ImageName("registry:5000/repository:tag")
    name shouldEqual ImageName(registry = Some("registry:5000"), namespace = None, repository = "repository", tag = Some("tag"))
  }

  it should "parse 'localhost/repository:tag'" in {
    val name = ImageName("localhost/repository:tag")
    name shouldEqual ImageName(registry = Some("localhost"), namespace = None, repository = "repository", tag = Some("tag"))
  }

  it should "parse 'namespace/repository:tag'" in {
    val name = ImageName("namespace/repository:tag")
    name shouldEqual ImageName(registry = None, namespace = Some("namespace"), repository = "repository", tag = Some("tag"))
  }

  it should "parse 'repository:tag'" in {
    val name = ImageName("repository:tag")
    name shouldEqual ImageName(registry = None, namespace = None, repository = "repository", tag = Some("tag"))
  }

  it should "parse 'repository'" in {
    val name = ImageName("repository")
    name shouldEqual ImageName(registry = None, namespace = None, repository = "repository", tag = None)
  }

  it should "not parse 'registry/namespace/repository'" in {
    a[IllegalArgumentException] should be thrownBy {
      ImageName("registry/namespace/repository")
    }
  }

  it should "produce correct strings" in {
    ImageName("test").toString shouldEqual "test"

    ImageName(namespace = Some("sbtdocker"), repository = "test").toString shouldEqual "sbtdocker/test"

    ImageName(repository = "test", tag = Some("v2")).toString shouldEqual "test:v2"

    ImageName(registry = Some("registry.example.com"), repository = "test").toString shouldEqual "registry.example.com/test"

    ImageName(
      registry = Some("registry.example.com"),
      namespace = Some("sbtdocker"),
      repository = "test",
      tag = Some("v2")
    ).toString shouldEqual "registry.example.com/sbtdocker/test:v2"
  }
}
