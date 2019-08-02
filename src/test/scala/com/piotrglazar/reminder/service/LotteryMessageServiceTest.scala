package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.client.LotteryClient
import org.mockito.BDDMockito.given
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class LotteryMessageServiceTest extends FlatSpec with Matchers with MockitoSugar {

  private val messageTemplate = "%s"

  private val client = mock[LotteryClient]
  private val threshold = 1000
  private val highPrize = 1234L
  private val lowPrize = 123L

  private val provider = new LotteryMessageService(client, threshold)

  it should "provide name" in {
    // given provider

    // when
    val name = provider.name

    // then
    name shouldBe "lottery"
  }

  it should "build message" in {
    // given
    given(client.fetchPrize()).willReturn(Future.successful(highPrize))

    // when
    val message = provider.buildMessage(messageTemplate)

    // then
    Await.result(message, 1 second).get shouldBe "1 234 zł"
  }

  it should "not build message if prize is lower than threshold" in {
    // given
    given(client.fetchPrize()).willReturn(Future.successful(lowPrize))

    // when
    val message = provider.buildMessage(messageTemplate)

    // then
    Await.result(message, 1 second) should be an 'empty
  }
}
