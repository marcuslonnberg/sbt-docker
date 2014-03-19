package sbtdocker

import sbt.IO
import sbt.file
import sbt.Path._
import Instructions._
import org.scalatest.{FunSuite, Matchers}

class DockerfileTest extends FunSuite with Matchers {
  test("Instructions are generated in sequence and correctly") {
    val d = new Dockerfile {
      addInstruction(From("image"))
      addInstruction(Maintainer("marcus"))
      addInstruction(Run("echo docker"))
      addInstruction(Cmd("cmd", "arg"))
      addInstruction(Expose(80))
      addInstruction(Env("key", "value"))
      addInstruction(Add("a", "b"))
      addInstruction(EntryPoint("entrypoint", "arg"))
      addInstruction(Volume("mountpoint"))
      addInstruction(User("marcus"))
      addInstruction(WorkDir("path"))
      addInstruction(OnBuild(Run("echo", "123")))
    }

    d.toString.lines.toList should contain theSameElementsInOrderAs List(
      "FROM image",
      "MAINTAINER marcus",
      "RUN echo docker",
      "CMD [\"cmd\", \"arg\"]",
      "EXPOSE 80",
      "ENV key value",
      "ADD a b",
      "ENTRYPOINT [\"entrypoint\", \"arg\"]",
      "VOLUME mountpoint",
      "USER marcus",
      "WORKDIR path",
      "ONBUILD RUN echo 123"
    )
  }

  test("Cmd and Entrypoint should handle \" in  arguments") {
    Cmd("\"").toInstructionString should equal( """CMD ["\""]""")

    EntryPoint("\"").toInstructionString should equal( """ENTRYPOINT ["\""]""")
  }

  test("add method should create a from path that is relative to the stage dir") {
    IO.withTemporaryDirectory {
      tempDir =>
        val d = new Dockerfile {
          implicit val stageDir = tempDir
          add(tempDir / "b.txt", file("c.txt"))
        }
        d.instructions should contain theSameElementsAs Seq(Add("b.txt", "c.txt"))
        d.pathsToCopy shouldBe empty
    }

    val d2 = new Dockerfile {
      implicit val stageDir = file("/e")
      add(file("/a/b/c/d.txt"), file("/c/d.txt"))
    }
    d2.instructions should contain theSameElementsAs Seq(Add("d.txt", "/c/d.txt"))
    d2.pathsToCopy should have length 1
  }

  test("add method should not create a relative path of the from path when there is no stage dir defined") {
    val d = new Dockerfile {
      add(file("/a/b/c/d.txt"), file("/c/d.txt"))
    }
    d.instructions should contain theSameElementsAs Seq(Add("a/b/c/d.txt", "/c/d.txt"))
    d.pathsToCopy shouldBe empty
  }

  test("onBuild method should generate a correct instruction string") {
    val d = new Dockerfile {
      onBuild(Run("echo", "123"))
    }
    d.toString should contain equals "ONBUILD RUN echo 123"
  }
}
