package com.piotrglazar.reminder.service

import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class LotteryMessageProviderTest extends FlatSpec with Matchers with MockitoSugar {

  private val messageTemplate = "%s"

  private val lotteryService = mock[LotteryService]

  private val provider = new LotteryMessageProvider(lotteryService)

  it should "provide name" in {
    // given provider

    // when
    val name = provider.name

    // then
    name shouldBe "lottery"
  }

  it should "build message" in {
    // given
    given(lotteryService.fetchPrize()).willReturn(Future.successful(1234))

    // when
    val message = provider.buildMessage(messageTemplate)

    // then
    Await.result(message, 1 second) shouldBe "1234"
  }
}
