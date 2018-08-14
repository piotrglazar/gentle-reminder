package com.piotrglazar.reminder.api

import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.piotrglazar.reminder.service.MessageSink
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class Routing(private val messageSink: MessageSink) extends PredefinedToResponseMarshallers with LazyLogging {

  val route: Route =
    path("health") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, """{"status": "OK"}"""))
      }
    } ~
    path("message") {
      post {
        entity(as[String]) { message =>
          onComplete(messageSink.sendMessage(message)) {
            case Success(_) =>
              logger.info(s"Message '$message' successfully sent")
              complete(StatusCodes.OK)
            case Failure(t) =>
              logger.error(s"Failed to send '$message'", t)
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
}
