package com.piotrglazar.reminder.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class LotteryClient(url: String)(private implicit val system: ActorSystem,
                                 private implicit val materializer: Materializer,
                                 private implicit val executionContext: ExecutionContextExecutor) {

  def fetchRawPage(): Future[String] = {
    val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    response.flatMap(_.entity.toStrict(1 second))
      .map(_.data.utf8String)
  }
}
