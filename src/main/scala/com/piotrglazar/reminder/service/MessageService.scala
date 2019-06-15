package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.client.{LotteryClient, LotteryPageParser}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

trait MessageService {

  def name: String

  def buildMessage(messageTemplate: String): Future[Option[String]]
}

class LotteryMessageService(private val client: LotteryClient, private val pageParser: LotteryPageParser,
                            private val threshold: Int)
                           (private implicit val executionContext: ExecutionContext)
  extends MessageService with LazyLogging {

  override def name: String = "lottery"

  override def buildMessage(messageTemplate: String): Future[Option[String]] = {
    client.fetchRawPage()
      .map(pageParser.parse(_).get)
      .map {
        case lowPrize if lowPrize < threshold =>
          logger.info(s"Fetched prize $lowPrize is lower than $threshold. Message will not be generated")
          None
        case prize =>
          Some(messageTemplate.format(prize))
      }
  }
}
