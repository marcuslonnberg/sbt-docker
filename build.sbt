sbtPlugin := true

name := "sbt-docker"
organization := "se.marcuslonnberg"
organizationHomepage := Some(url("https://github.com/marcuslonnberg"))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.apache.commons" % "commons-lang3" % "3.5",
  "com.spotify" % "docker-client" % "7.0.2"
)

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

licenses := Seq("MIT License" -> url("https://github.com/marcuslonnberg/sbt-docker/blob/master/LICENSE"))
homepage := Some(url("https://github.com/marcuslonnberg/sbt-docker"))
scmInfo := Some(ScmInfo(url("https://github.com/marcuslonnberg/sbt-docker"), "scm:git:git://github.com:marcuslonnberg/sbt-docker.git"))

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false}

pomExtra := {
  <developers>
    <developer>
      <id>marcuslonnberg</id>
      <name>Marcus Lönnberg</name>
      <url>http://marcuslonnberg.se</url>
    </developer>
  </developers>
}

useGpg := true
