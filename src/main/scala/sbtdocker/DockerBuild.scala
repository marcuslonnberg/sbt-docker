package sbtdocker

import java.util.concurrent.LinkedBlockingQueue

import sbt._
import staging.{DockerfileProcessor, StagedDockerfile}

import scala.sys.process.{Process, ProcessBuilder, ProcessLogger}

object DockerBuild {
  /**
   * Build a Dockerfile using a provided docker binary.
   *
   * @param dockerfile Dockerfile to build
   * @param processor processor to create a staging directory for the Dockerfile
   * @param imageNames names of the resulting image
   * @param stageDir stage dir
   * @param dockerPath path to the docker binary
   * @param buildOptions options for the build command
   * @param log logger
   */
  def apply(dockerfile: DockerfileLike, processor: DockerfileProcessor, imageNames: Seq[ImageName],
            buildOptions: BuildOptions, stageDir: File, dockerPath: String, log: Logger): ImageId = {
    val staged = processor(dockerfile, stageDir)

    apply(staged, imageNames, buildOptions, stageDir, dockerPath, log)
  }

  /**
   * Build a Dockerfile using a provided docker binary.
   *
   * @param staged a staged Dockerfile to build.
   * @param imageNames names of the resulting image
   * @param stageDir stage dir
   * @param dockerPath path to the docker binary
   * @param buildOptions options for the build command
   * @param log logger
   */
  def apply(staged: StagedDockerfile, imageNames: Seq[ImageName], buildOptions: BuildOptions, stageDir: File, dockerPath: String, log: Logger): ImageId = {
    log.debug("Building Dockerfile:\n" + staged.instructionsString)

    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    clean(stageDir)
    createDockerfile(staged, stageDir)
    prepareFiles(staged)
    buildAndTag(imageNames, stageDir, dockerPath, buildOptions, log)
  }

  private[sbtdocker] def clean(stageDir: File) = {
    IO.delete(stageDir)
  }

  private[sbtdocker] def createDockerfile(staged: StagedDockerfile, stageDir: File) = {
    IO.write(stageDir / "Dockerfile", staged.instructionsString)
  }

  private[sbtdocker] def prepareFiles(staged: StagedDockerfile) = {
    staged.stageFiles.foreach {
      case (source, destination) =>
        source.stage(destination)
    }
  }

  private val SuccessfullyBuilt = "^Successfully built ([0-9a-f]+)$".r
  private val SuccessfullyBuiltBuildKit = "^.*writing image sha256:([0-9a-f]+).*done$".r

  private[sbtdocker] trait ProcessLoggerWithStream extends ProcessLogger {
    val queue = new LinkedBlockingQueue[Either[Int, String]]
    val log: Logger

    override def out(s: => String): Unit = fn(s)
    override def err(s: => String): Unit = fn(s)
    override def buffer[T](f: => T): T = f

    def stream = next()

    def forBuilder(processBuilder: ProcessBuilder) {
      val self: ProcessLoggerWithStream = this

      new Thread {
        self.end(processBuilder.run(self).exitValue())
      }.start()
    }

    private def end = { exitCode: Int ⇒ queue.put(Left(exitCode)) }

    private def fn = { line: String =>
      log.info(line)
      queue.put(Right(line))
    }

    private def next(): Stream[String] = queue.take match {
      case Right(s) => Stream.cons(s, next())
      case Left(_) => Stream.empty
    }
  }

  private[sbtdocker] object ProcessLoggerWithStream {
    def apply(logger: Logger): ProcessLoggerWithStream = new ProcessLoggerWithStream {
      override val log: Logger = logger
    }
  }

  private[sbtdocker] def buildAndTag(imageNames: Seq[ImageName], stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger): ImageId = {
    val processLogger = ProcessLoggerWithStream(log)

    val imageId = build(stageDir, dockerPath, buildOptions, log, processLogger)

    imageNames.foreach { name =>
      DockerTag(imageId, name, dockerPath, log)
    }
    
    imageId
  }

  private[sbtdocker] def build(stageDir: File, dockerPath: String, buildOptions: BuildOptions, log: Logger, processLogger: ProcessLoggerWithStream): ImageId = {
    val flags = buildFlags(buildOptions)
    val command = dockerPath :: "build" :: flags ::: "." :: Nil
    log.debug(s"Running command: '${command.mkString(" ")}' in '${stageDir.absString}'")

    processLogger.forBuilder(Process(command, stageDir))

    val imageId = processLogger.stream.collect {
      case SuccessfullyBuilt(id)         => ImageId(id)
      case SuccessfullyBuiltBuildKit(id) => ImageId(id)
    }.lastOption

    imageId match {
      case Some(id) =>
        id
      case None =>
        sys.error("Could not parse image id")
    }
  }

  private[sbtdocker] def buildFlags(buildOptions: BuildOptions): List[String] = {
    val cacheFlag = "--no-cache=" + !buildOptions.cache
    val removeFlag = {
      buildOptions.removeIntermediateContainers match {
        case BuildOptions.Remove.Always =>
          "--force-rm=true"
        case BuildOptions.Remove.Never =>
          "--rm=false"
        case BuildOptions.Remove.OnSuccess =>
          "--rm=true"
      }
    }
    val pullFlag = {
      val value = buildOptions.pullBaseImage match {
        case BuildOptions.Pull.Always => true
        case BuildOptions.Pull.IfMissing => false
      }
      "--pull=" + value
    }

    cacheFlag :: removeFlag :: pullFlag :: Nil
  }
}
