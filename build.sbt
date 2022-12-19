import Libraries._
import sbtrelease.ReleaseStateTransformations._

lazy val root = (project in file("."))
  .settings(
    name := "gentle-reminder",
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      Utils.guava,
      Utils.logback,
      Utils.config,
      Utils.pimpedConfig,
      Utils.logging,
      Utils.slackClient,
      Utils.scheduling,
      Utils.akkaHttpCirce,
      TestUtils.scalatest,
      TestUtils.mockito,
      TestUtils.restito
    ) ++ Core.akka ++ Core.akkaTest ++ Utils.circe,
    organization := "com.piotrglazar",
    publishMavenStyle := false,
    publishArtifact := false,
    skip in publish := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps", "-encoding", "utf8",
      "-Xfatal-warnings")
  )

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
