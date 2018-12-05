sbtPlugin := true

name := "sbt-docker"
organization := "se.marcuslonnberg"
organizationHomepage := Some(url("https://github.com/marcuslonnberg"))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.apache.commons" % "commons-lang3" % "3.8.1",
  "com.spotify" % "docker-client" % "8.14.5",
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"
)

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

publishMavenStyle := true

licenses := Seq("MIT License" -> url("https://github.com/marcuslonnberg/sbt-docker/blob/master/LICENSE"))
homepage := Some(url("https://github.com/marcuslonnberg/sbt-docker"))
scmInfo := Some(
  ScmInfo(browseUrl = url("https://github.com/marcuslonnberg/sbt-docker"), connection = "scm:git:git://github.com:marcuslonnberg/sbt-docker.git")
)
developers := List(
  Developer(id = "marcuslonnberg", name = "Marcus Lönnberg", email = "", url = url("http://marcuslonnberg.se"))
)

publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
  else Opts.resolver.sonatypeStaging
)

useGpg := true
