package com.piotrglazar.reminder.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.piotrglazar.reminder.util.PageFetchExceptions.{InternalServerErrorException, PageNotFoundException, UnexpectedStatusCodeException}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class LotteryClient(private val url: String)(private implicit val system: ActorSystem,
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
}
