package com.piotrglazar.reminder.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.piotrglazar.reminder.api.lottery.LotteryJson.Draws
import com.piotrglazar.reminder.util.PageFetchExceptions.{InternalServerErrorException, PageNotFoundException, UnexpectedStatusCodeException}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class LotteryClient(private val apiUrl: String)(private implicit val system: ActorSystem,
                                 private implicit val materializer: Materializer,
                                 private implicit val executionContext: ExecutionContextExecutor) extends LazyLogging {

  def fetchPrize(): Future[Long] = {
    Http().singleRequest(HttpRequest(uri = apiUrl))
      .flatMap { r =>
        r.status match {
          case OK =>
            logger.info(s"Fetching from $apiUrl - OK")
            Future.successful(r)
          case NotFound =>
            logger.error(s"Fetching from $apiUrl - NotFound")
            throw new PageNotFoundException(apiUrl)
          case InternalServerError =>
            logger.error(s"Fetching from $apiUrl - InternalServerError")
            throw new InternalServerErrorException(apiUrl)
          case other =>
            logger.error(s"Unexpected status code $other")
            throw new UnexpectedStatusCodeException(other.intValue(), apiUrl)
        }
      }
      .flatMap(Unmarshal(_).to[Draws])
      .map(_.draws.head)
      // convert from coins
      .map(_.estimatedJackpot / 100)
  }
}
