package com.piotrglazar.reminder.service

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class LoggingMessageSink extends MessageSink with LazyLogging {

  override def sendMessage(message: String): Future[Unit] = {
    Future.successful(logger.info(s"Received message: '$message'"))
  }

  override def name: MessageSink.SinkName = MessageSink.SinkName("logging")
}
