package com.piotrglazar.reminder.service

import scala.concurrent.{ExecutionContextExecutor, Future}

trait MessageProvider {

  def name: String

  def buildMessage(messageTemplate: String): Future[String]
}

class LotteryMessageProvider(private val lotteryService: LotteryService)
                            (private implicit val executionContextExecutor: ExecutionContextExecutor)
  extends MessageProvider {

  override def name: String = "lottery"

  override def buildMessage(messageTemplate: String): Future[String] = {
    lotteryService.fetchPrize()
      .map(prize => messageTemplate.format(prize))
  }
}


