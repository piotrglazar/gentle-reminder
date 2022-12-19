import sbt._

object Libraries {

  object Core {
    private val akkaVersion = "2.7.0"
    private val akkaHttpVersion = "10.4.0"

    lazy val akka: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    )

    lazy val akkaTest: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
    )
  }

  object Utils {
    private val circeVersion = "0.13.0"
    
    lazy val guava: ModuleID = "com.google.guava" % "guava" % "29.0-jre"
    lazy val logback: ModuleID = "ch.qos.logback" % "logback-classic" % "1.3.0-alpha4"
    lazy val config: ModuleID = "com.typesafe" % "config" % "1.3.4"
    lazy val pimpedConfig: ModuleID = "com.github.kxbmap" %% "configs" % "0.6.1"
    lazy val logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    lazy val scheduling: ModuleID = "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.3-akka-2.6.x"
    lazy val slackClient: ModuleID = "com.github.slack-scala-client" %% "slack-scala-client" % "0.4.1"
    lazy val circe: Seq[ModuleID] = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
    lazy val akkaHttpCirce: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.40.0-RC3"
  }

  object TestUtils {
    lazy val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.8" % Test
    lazy val mockito: ModuleID = "org.mockito" % "mockito-core" % "2.28.2" % Test
    lazy val restito: ModuleID = "com.xebialabs.restito" % "restito" % "0.9.3" % Test
  }

}
