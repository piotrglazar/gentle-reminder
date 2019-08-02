package com.piotrglazar.reminder.service

import java.text.NumberFormat
import java.util.Locale

import com.piotrglazar.reminder.client.LotteryClient
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

trait MessageService {

  def name: String

  def buildMessage(messageTemplate: String): Future[Option[String]]
}

class LotteryMessageService(private val client: LotteryClient, private val threshold: Int)
                           (private implicit val executionContext: ExecutionContext)
  extends MessageService with LazyLogging {

  private val numberFormat = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"))

  override def name: String = "lottery"

  override def buildMessage(messageTemplate: String): Future[Option[String]] = {
    client.fetchPrize()
      .map {
        case lowPrize if lowPrize < threshold =>
          logger.info(s"Fetched prize $lowPrize is lower than $threshold. Message will not be generated")
          None
        case prize =>
          val prettyPrize = numberFormat.format(prize).replace(",00", "")
          Some(messageTemplate.format(prettyPrize))
      }
  }
}
