package com.piotrglazar.reminder.client

import com.piotrglazar.reminder.TestUtils
import org.scalatest.{FlatSpec, Matchers}

class LotteryPageParserTest extends FlatSpec with Matchers {

  private lazy val pageWithDefaultPrize = TestUtils.readResource("/lotto-default-prize.html")

  private lazy val pageWithHigherPrize = TestUtils.readResource("/lotto-higher-prize.html")

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
}
