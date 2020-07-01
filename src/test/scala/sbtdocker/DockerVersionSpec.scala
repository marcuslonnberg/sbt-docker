package sbtdocker

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DockerVersionSpec extends AnyFlatSpec with Matchers {
  it should "correctly parse" in {
    DockerVersion.parseVersion("1.0.0") shouldEqual DockerVersion(1,0,0)
    DockerVersion.parseVersion("11.1.1") shouldEqual DockerVersion(11,1,1)
    DockerVersion.parseVersion("1.0.0-SNAPSHOT") shouldEqual DockerVersion(1,0,0)
    DockerVersion.parseVersion("1.2.3") shouldEqual DockerVersion(1,2,3)
    DockerVersion.parseVersion("1.2.3-rc3") shouldEqual DockerVersion(1,2,3)
    DockerVersion.parseVersion("17.03.0-ce") shouldEqual DockerVersion(17,3,0)
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
