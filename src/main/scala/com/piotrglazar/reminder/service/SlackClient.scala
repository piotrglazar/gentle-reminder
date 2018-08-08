package com.piotrglazar.reminder.service

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import slack.api.SlackApiClient

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class SlackClient(token: String, private val channelId: String)
                 (implicit private val system: ActorSystem, private val context: ExecutionContext) extends LazyLogging {

  private val slack = new SlackApiClient(token)

  def sendMessage(message: String): Future[Unit] = {
    val result = slack.postChatMessage(channelId, message)
    result.onComplete {
      case Success(r) =>
        logger.debug(s"Successfully sent message with timestamp $r")
      case Failure(t) =>
        logger.info("Failed to send message", t)
    }

    result.map(_ => (): Unit)
  }
}
