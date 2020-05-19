package com.piotrglazar.reminder.api

import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.piotrglazar.reminder.api.Messages.SlackMessage
import com.piotrglazar.reminder.service.{MessageSink, SecurityService}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.util.{Failure, Success}

class Routing(private val messageSink: MessageSink, private val securityService: SecurityService)
  extends PredefinedToResponseMarshallers with LazyLogging {

  val route: Route =
    path("health") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, """{"status": "OK"}"""))
      }
    } ~
    path("message") {
      post {
        entity(as[SlackMessage]) { message =>
          if (securityService.passwordMatches(message.password)) {
            onComplete(messageSink.sendMessage(message.messageBody)) {
              case Success(_) =>
                logger.info(s"Message '$message' successfully sent")
                complete(StatusCodes.OK)
              case Failure(t) =>
                logger.error(s"Failed to send '$message'", t)
                complete(StatusCodes.InternalServerError)
            }
          } else {
            logger.error(s"Invalid user password: [${message.password}]")
            complete(StatusCodes.NotFound)
          }
        }
      }
    }
}
