import Libraries._

lazy val root = (project in file("."))
  .settings(
    name := "gentle-reminder",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      Utils.guava,
      Utils.logback,
      Utils.config,
      Utils.pimpedConfig,
      Utils.logging,
      Utils.slackClient,
      Utils.scheduling,
      TestUtils.scalatest,
      TestUtils.mockito
    ) ++ Core.akka ++ Core.akkaTest,
    version := "0.1.0",
    organization := "com.piotrglazar",
    publishMavenStyle := true
  )

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
