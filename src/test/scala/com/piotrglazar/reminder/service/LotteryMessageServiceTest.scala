package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.client.{LotteryClient, LotteryPageParser}
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

class LotteryMessageServiceTest extends FlatSpec with Matchers with MockitoSugar {

  private val messageTemplate = "%s"

  private val client = mock[LotteryClient]
  private val parser = mock[LotteryPageParser]
  private val threshold = 1000

  private val provider = new LotteryMessageService(client, parser, threshold)

  it should "provide name" in {
    // given provider

    // when
    val name = provider.name

    // then
    name shouldBe "lottery"
  }

  it should "build message" in {
    // given
    given(client.fetchRawPage()).willReturn(Future.successful("page"))
    given(parser.parse("page")).willReturn(Success(1234))

    // when
    val message = provider.buildMessage(messageTemplate)

    // then
    Await.result(message, 1 second).get shouldBe "1234"
  }

  it should "not build message if prize is lower than threshold" in {
    // given
    given(client.fetchRawPage()).willReturn(Future.successful("page"))
    given(parser.parse("page")).willReturn(Success(threshold - 1))

    // when
    val message = provider.buildMessage(messageTemplate)

    // then
    Await.result(message, 1 second) should be an 'empty
  }
}
