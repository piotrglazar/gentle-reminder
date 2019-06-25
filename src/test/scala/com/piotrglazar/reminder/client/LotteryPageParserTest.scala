package com.piotrglazar.reminder.client

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class LotteryPageParserTest extends FlatSpec with Matchers {

  private lazy val pageWithDefaultPrize = readPage("/lotto-default-prize.html")

  private lazy val pageWithHigherPrize = readPage("/lotto-higher-prize.html")

  private val parser = new LotteryPageParser

  it should "extract default prize" in {
    // given page read

    // when
    val result = parser.parse(pageWithDefaultPrize)

    // then
    result should be a 'success
    result.get shouldBe 2000000
  }

  it should "extract higher prize" in {
    // given page read

    // when
    val result = parser.parse(pageWithHigherPrize)

    // then
    result should be a 'success
    result.get shouldBe 6000000
  }

  private def readPage(resourceName: String): String =
    Source.fromFile(getClass.getResource(resourceName).toURI, "UTF-8").mkString
}
