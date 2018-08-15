package com.piotrglazar.reminder.config

import com.piotrglazar.reminder.config.ReminderConfig.{JobConfig, RunConfig, SlackConfig}
import com.piotrglazar.reminder.service.MessageSink.SinkName
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import configs.{Configs, Result}

object ReminderConfig extends LazyLogging {
  case class RunConfig(host: String, port: Int)
  case class SlackConfig(token: String, channelId: String)
  case class JobConfig(name: String, sink: String, message: String)

  def read(): Result[ReminderConfig] = {
    val config = ConfigFactory.load()

    for {
      runConfig <- Configs[RunConfig].get(config, "app")
      slackConfig <- Configs[SlackConfig].get(config, "slack")
      jobs <- Configs[List[JobConfig]].get(config, "jobs")
    } yield {
      new ReminderConfig(runConfig, slackConfig, jobs)
    }
  }
}

case class ReminderConfig(runConfig: RunConfig, slackConfig: SlackConfig, jobs: List[JobConfig])
