package com.piotrglazar.reminder.service

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class MessageServiceRegistry(providers: List[MessageService]) extends LazyLogging {

  private val providersByName = providers.groupBy(_.name).mapValues(_.head).map(identity)

  def buildMessage(jobName: String, messageTemplate: String,
                   messageProviderName: Option[String]): Future[Option[String]] = {
    messageProviderName match {
      case None =>
        logger.info(s"job $jobName uses message $messageTemplate")
        Future.successful(Some(messageTemplate))
      case Some(name) if providersByName.contains(name) =>
        logger.info(s"job $jobName uses provider $name")
        providersByName(name).buildMessage(messageTemplate)
      case Some(name) =>
        val msg = s"job $jobName uses provider $name, but no such provider was found!"
        logger.error(msg)
        Future.failed(new RuntimeException(msg))
    }
  }
}
