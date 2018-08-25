package com.piotrglazar.reminder.service
import scala.concurrent.Future

class SlackMessageSink(private val slackClient: SlackClient) extends MessageSink {

  override def sendMessage(message: String, users: List[String]): Future[Unit] = {
    slackClient.sendMessage(buildUserMentions(users) + message)
  }

  private def buildUserMentions(users: List[String]): String = {
    users.foldLeft("") { case (acc, i) =>
      acc +  s"<@$i> "
    }
  }

  override def name: MessageSink.SinkName = MessageSink.SinkName("slack")
}
