package com.piotrglazar.reminder.client

import com.piotrglazar.reminder.client.LotteryPageParser.prizeSelector
import org.jsoup.Jsoup

import scala.util.Try

object LotteryPageParser {
  private val prizeSelector = "div.jackpot-amount"
}

class LotteryPageParser {

  def parse(rawHtml: String): Try[Int] = Try {
    val page = Jsoup.parse(rawHtml)
    val rawPrizeText = page.select(prizeSelector).first().text().trim
    rawPrizeText.filter(Character.isDigit).toInt
  }
}
