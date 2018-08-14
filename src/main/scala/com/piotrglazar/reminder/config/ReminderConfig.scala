package com.piotrglazar.reminder.config

import com.piotrglazar.reminder.config.ReminderConfig.{RunConfig, SlackConfig}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import configs.{Configs, Result}

object ReminderConfig extends LazyLogging {
  case class RunConfig(host: String, port: Int)
  case class SlackConfig(token: String, channelId: String)

  def read(): Result[ReminderConfig] = {
    val config = ConfigFactory.load()

    for {
      runConfig <- Configs[RunConfig].get(config, "app")
      slackConfig <- Configs[SlackConfig].get(config, "slack")
    } yield {
      new ReminderConfig(runConfig, slackConfig)
    }
  }
}

case class ReminderConfig(runConfig: RunConfig, slackConfig: SlackConfig)
