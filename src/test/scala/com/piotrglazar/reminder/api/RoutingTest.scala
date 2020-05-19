package com.piotrglazar.reminder.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import com.piotrglazar.reminder.api.Messages.SlackMessage
import com.piotrglazar.reminder.service.{MessageSink, SecurityService}
import org.mockito.BDDMockito.given
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future

class RoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with MockitoSugar with BeforeAndAfter
  // needed for unmarshalling as string
  with PredefinedFromEntityUnmarshallers {

  private var messageSink: MessageSink = _

  private var securityService: SecurityService = _

  private var routing: Routing = _

  before {
    messageSink = mock[MessageSink]
    securityService = mock[SecurityService]
    routing = new Routing(messageSink, securityService)
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
    val message = SlackMessage("pass", "hey")
    given(securityService.passwordMatches(message.password)).willReturn(true)
    given(messageSink.sendMessage(message.messageBody)).willReturn(Future.successful((): Unit))

    // when
    Post("/message", message) ~> routing.route ~> check {

      // then
      status shouldEqual StatusCodes.OK
    }
  }

  it should "return http 404 when password is invalid" in {
    // given
    val message = SlackMessage("pass", "hey")
    given(securityService.passwordMatches(message.password)).willReturn(false)

    // when
    Post("/message", message) ~> routing.route ~> check {

      // then
      status shouldEqual StatusCodes.NotFound
    }
  }
}
