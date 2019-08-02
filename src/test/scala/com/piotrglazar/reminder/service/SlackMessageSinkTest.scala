package com.piotrglazar.reminder.service

import org.mockito.BDDMockito.given
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

class SlackMessageSinkTest extends FlatSpec with MockitoSugar with BeforeAndAfter with ScalaFutures {

  private var client: SlackClient = _

  private var sink: SlackMessageSink = _

  before {
    client = mock[SlackClient]
    sink = new SlackMessageSink(client)
  }

  it should "just forward a message when there are no user mentions" in {
    // given
    val msg = "message"
    val users = List.empty
    given(client.sendMessage("message")).willReturn(Future.successful((): Unit))

    // when
    val result = sink.sendMessage(msg, users)

    // then
    result.futureValue
  }

  it should "build message with one user mentioned" in {
    // given
    val msg = "message"
    val users = List("userA")
    given(client.sendMessage("<@userA> message")).willReturn(Future.successful((): Unit))

    // when
    val result = sink.sendMessage(msg, users)

    // then
    result.futureValue
  }

  it should "build message with two users mentioned" in {
    // given
    val msg = "message"
    val users = List("userA", "userB")
    given(client.sendMessage("<@userA> <@userB> message")).willReturn(Future.successful((): Unit))

    // when
    val result = sink.sendMessage(msg, users)

    // then
    result.futureValue
  }
}
