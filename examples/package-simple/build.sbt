import java.nio.file.Paths
import sbtdocker.{ImageName, Dockerfile}
import DockerKeys._
import scala.Some

name := "example-package-simple"

organization := "sbtdocker"

version := "0.1.0"

dockerSettings

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package` in(Compile, packageBin))

// Define a Dockerfile
dockerfile in docker <<= (artifactPath.in(Compile, packageBin), managedClasspath in Compile, mainClass.in(Compile, packageBin)) map {
  case (jarFile, classpath, Some(mainClass)) => new Dockerfile {
    from("dockerfile/java")
    val files = classpath.files.map { file =>
      val target = "/app/" + file.getName
      add(file, target)
      target
    }
    // Add the generated jar file
    val jarTarget = Paths.get("/app", jarFile.getName)
    add(jarFile, jarTarget)
    // Run the jar file with scala library on the class path
    val classpathString = files.mkString(":") + ":" + jarTarget.toString
    entryPoint("java", "-cp", classpathString, mainClass)
  }
}

// Set a custom image name
imageName in docker <<= (organization, name, version) map {(organization, name, version) =>
  ImageName(namespace = Some(organization), repository = name, tag = Some("v" + version))
}
