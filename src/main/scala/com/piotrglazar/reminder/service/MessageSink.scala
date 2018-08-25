package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.service.MessageSink.SinkName

import scala.concurrent.Future

object MessageSink {
  case class SinkName(value: String) extends AnyVal
}

trait MessageSink {

  def name: SinkName

  def sendMessage(message: String, users: List[String] = List.empty): Future[Unit]
}
