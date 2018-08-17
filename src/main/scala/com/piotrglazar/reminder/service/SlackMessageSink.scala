package com.piotrglazar.reminder.service
import scala.concurrent.Future

class SlackMessageSink(private val slackClient: SlackClient) extends MessageSink {

  override def sendMessage(message: String): Future[Unit] = {
    slackClient.sendMessage(message)
  }

  override def name: MessageSink.SinkName = MessageSink.SinkName("slack")
}
