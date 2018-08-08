package com.piotrglazar.reminder.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import com.piotrglazar.reminder.service.MessageSink
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.Future

class RoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with MockitoSugar with BeforeAndAfter
  // needed for unmarshalling as string
  with PredefinedFromEntityUnmarshallers {

  private var messageSink: MessageSink = _

  private var routing: Routing = _

  before {
    messageSink = mock[MessageSink]
    routing = new Routing(messageSink)
  }

  it should "accept health check" in {
    // when
    Get("/health") ~> routing.route ~> check {

      // then
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual """{"status": "OK"}"""
    }
  }

  it should "send message to sink" in {
    // given
    val message = "hey"
    given(messageSink.sendMessage(message)).willReturn(Future.successful((): Unit))

    // when
    Post("/message", message) ~> routing.route ~> check {

      // then
      status shouldEqual StatusCodes.OK
    }
  }
}
