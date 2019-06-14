package com.piotrglazar.reminder.config

import com.piotrglazar.reminder.config.ReminderConfig.{BusinessConfig, JobConfig, RunConfig, SlackConfig, UserConfig}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import configs.{Configs, Result}

object ReminderConfig extends LazyLogging {
  case class RunConfig(host: String, port: Int)
  case class SlackConfig(token: String, channelId: String)
  case class JobConfig(name: String, sink: String, message: String, users: List[String],
                       messageProvider: Option[String] = None)
  case class UserConfig(name: String, slackName: String)
  case class BusinessConfig(lotteryUrl: String)

  def read(): Result[ReminderConfig] = {
    val config = ConfigFactory.load()

    for {
      runConfig <- Configs[RunConfig].get(config, "app")
      slackConfig <- Configs[SlackConfig].get(config, "slack")
      businessConfig <- Configs[BusinessConfig].get(config, "business")
      jobs <- Configs[List[JobConfig]].get(config, "jobs")
      users <- Configs[List[UserConfig]].get(config, "users")
    } yield {
      ReminderConfig(runConfig, slackConfig, businessConfig, jobs, users)
    }
  }
}

case class ReminderConfig(runConfig: RunConfig, slackConfig: SlackConfig, businessConfig: BusinessConfig,
                          jobs: List[JobConfig], users: List[UserConfig])
