package sbtdocker

import org.scalatest.{FlatSpec, Matchers}

class DockerVersionSpec extends FlatSpec with Matchers {
  it should "correctly parse" in {
    DockerVersion.parseVersion("1.0.0") shouldEqual DockerVersion(1,0,0)
    DockerVersion.parseVersion("11.1.1") shouldEqual DockerVersion(11,1,1)
    DockerVersion.parseVersion("1.0.0-SNAPSHOT") shouldEqual DockerVersion(1,0,0)
    DockerVersion.parseVersion("1.2.3") shouldEqual DockerVersion(1,2,3)
    DockerVersion.parseVersion("1.2.3-rc3") shouldEqual DockerVersion(1,2,3)
  }

  it should "not parse" in {
    intercept[RuntimeException] {
      DockerVersion.parseVersion("")
    }

    intercept[RuntimeException] {
      DockerVersion.parseVersion("1.0")
    }

    intercept[RuntimeException] {
      DockerVersion.parseVersion("-1.0")
    }

    intercept[RuntimeException] {
      DockerVersion.parseVersion("version")
    }
  }
}
