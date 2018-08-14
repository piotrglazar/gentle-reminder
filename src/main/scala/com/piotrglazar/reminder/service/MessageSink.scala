package com.piotrglazar.reminder.service

import scala.concurrent.Future

trait MessageSink {

  def sendMessage(message: String): Future[Unit]
}
