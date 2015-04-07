import sbtrelease._
import ReleaseStateTransformations._
import ReleaseKeys._

releaseSettings

publishArtifactsAction := PgpKeys.publishSigned.value

lazy val runScriptedTests = taskKey[Unit]("Run all scripted tests")
runScriptedTests := scripted.toTask("").value
lazy val runScripted: ReleaseStep = releaseTask(runScriptedTests in ThisProject)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  runScripted,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
