package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.client.{LotteryClient, LotteryPageParser}

import scala.concurrent.{ExecutionContextExecutor, Future}

class LotteryService(private val client: LotteryClient, private val pageParser: LotteryPageParser)
                    (private implicit val executionContextExecutor: ExecutionContextExecutor) {

  def fetchPrize(): Future[Int] = {
    client.fetchRawPage()
      .map(pageParser.parse(_).get)
  }
}
