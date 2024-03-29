package com.piotrglazar.reminder.service

import akka.actor.{Actor, Props}
import akka.event.Logging.InfoLevel
import akka.event.LoggingReceive
import com.piotrglazar.reminder.config.ReminderConfig.JobConfig
import com.piotrglazar.reminder.service.MessageSink.SinkName
import com.piotrglazar.reminder.service.Worker.{SendResult, Tick}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

object Worker {

  case class Tick(jobName: String)

  case class SendResult(jobName: String, result: Try[Unit])

  def props(sinks: List[MessageSink], jobs: List[JobConfig], userService: UserService,
            registry: MessageServiceRegistry): Props =
    Props(new Worker(sinks, jobs, userService, registry))
}

class Worker(sinks: List[MessageSink], jobs: List[JobConfig], private val userService: UserService,
             private val registry: MessageServiceRegistry) extends Actor with LazyLogging {

  private implicit val executor: ExecutionContextExecutor = context.dispatcher

  private val sinkByName = sinks.groupBy(_.name).view.mapValues(_.head)

  private val jobByName = jobs.groupBy(_.name).view.mapValues(_.head)

  override def receive: Receive = LoggingReceive(InfoLevel)(workerReceive)

  private def workerReceive: Receive = {
    case Tick(jobName) =>
      for {
        job <- jobByName.get(jobName)
        sink <- sinkByName.get(SinkName(job.sink))
      } {
        val users = job.users.flatMap(userService.getExternalUserName)
        val message = registry.buildMessage(jobName, job.message, job.messageProvider)
        message.foreach(_.foreach(m => sink.sendMessage(m, users).onComplete(r => self ! SendResult(jobName, r))))
      }
    case SendResult(jobName, result) =>
      result match {
        case Success(_) =>
          logger.info(s"Successfully sent message for $jobName")
        case Failure(t) =>
          logger.error(s"Failed to send message for $jobName", t)
      }
  }
}
