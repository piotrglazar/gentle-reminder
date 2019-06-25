package com.piotrglazar.reminder.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.piotrglazar.reminder.api.lottery.LotteryJson.{Draw, Draws}
import com.piotrglazar.reminder.util.PageFetchExceptions.{InternalServerErrorException, PageNotFoundException, UnexpectedStatusCodeException}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class LotteryClient(private val url: String, private val apiUrl: String)(private implicit val system: ActorSystem,
                                 private implicit val materializer: Materializer,
                                 private implicit val executionContext: ExecutionContextExecutor) extends LazyLogging {

  def fetchRawPage(): Future[String] = {
    val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    response
      .flatMap { r =>
        r.status match {
          case OK =>
            logger.info(s"Fetching from $url - OK")
            r.entity.toStrict(10 seconds)
          case NotFound =>
            logger.error(s"Fetching from $url - NotFound")
            throw new PageNotFoundException(url)
          case InternalServerError =>
            logger.error(s"Fetching from $url - InternalServerError")
            throw new InternalServerErrorException(url)
          case other =>
            logger.error(s"Unexpected status code $other")
            throw new UnexpectedStatusCodeException(other.intValue(), url)
        }
      }
      .map(_.data.utf8String)
  }

  def fetchPrize(): Future[Long] = {
    Http().singleRequest(HttpRequest(uri = url))
      .flatMap(Unmarshal(_).to[Draws])
      .map(_.draws.head)
      // convert from coins
      .map(_.estimatedJackpot / 100)
  }
}
