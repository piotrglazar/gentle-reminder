package com.piotrglazar.reminder.client

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class LotteryPageParserTest extends FlatSpec with Matchers {

  private lazy val pageWithDefaultPrize = Source.fromFile(getClass.getResource("/lotto-default-prize.html").toURI, "UTF-8").mkString

  private val parser = new LotteryPageParser

  it should "extract default prize" in {
    // given page read

    // when
    val result = parser.parse(pageWithDefaultPrize)
    result should be a 'success
    result.get == 2000000
  }

}
