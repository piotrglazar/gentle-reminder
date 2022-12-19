package com.piotrglazar.reminder.config

import com.piotrglazar.reminder.config.ReminderConfig.{BusinessConfig, CertConfig, JobConfig, RunConfig, SlackConfig, UserConfig}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import configs.{ConfigReader, Configs, Result}

object ReminderConfig extends LazyLogging {
  case class RunConfig(host: String, port: Int, maintenancePassword: String)
  case class SlackConfig(token: String, channelId: String)
  case class CertConfig(password: String)
  case class JobConfig(name: String, sink: String, message: String, users: List[String],
                       messageProvider: Option[String] = None)
  case class UserConfig(name: String, slackName: String)
  case class BusinessConfig(lotteryThreshold: Int, lotteryApiUrl: String)

  def read(): Result[ReminderConfig] = {
    val config = ConfigFactory.load()

    for {
      runConfig <- ConfigReader[RunConfig].read(config, "app")
      slackConfig <- ConfigReader[SlackConfig].read(config, "slack")
      businessConfig <- ConfigReader[BusinessConfig].read(config, "business")
      jobs <- ConfigReader[List[JobConfig]].read(config, "jobs")
      users <- ConfigReader[List[UserConfig]].read(config, "users")
      cert <- ConfigReader[CertConfig].read(config, "cert")
    } yield {
      ReminderConfig(runConfig, slackConfig, businessConfig, jobs, users, cert)
    }
  }
}

case class ReminderConfig(runConfig: RunConfig, slackConfig: SlackConfig, businessConfig: BusinessConfig,
                          jobs: List[JobConfig], users: List[UserConfig], certConfig: CertConfig)
