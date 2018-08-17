package com.piotrglazar.reminder.service

import akka.actor.{ActorRef, ActorSystem}
import com.piotrglazar.reminder.config.ReminderConfig.JobConfig
import com.piotrglazar.reminder.service.Worker.Tick
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.scalalogging.LazyLogging

object SchedulingService extends LazyLogging {

  def startScheduling(system: ActorSystem, jobs: List[JobConfig], receiver: ActorRef): Unit = {
    val quartz = QuartzSchedulerExtension(system)

    jobs.foreach { config =>
      val launchDate = quartz.schedule(config.name, receiver, Tick(config.name))
      logger.info(s"Job '${config.name}' will run at $launchDate")
    }
  }
}
