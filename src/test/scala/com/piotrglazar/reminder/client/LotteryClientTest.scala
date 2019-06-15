package com.piotrglazar.reminder.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.piotrglazar.reminder.util.PageFetchExceptions._
import com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import com.xebialabs.restito.semantics.Action.{resourceContent, status}
import com.xebialabs.restito.semantics.Condition
import com.xebialabs.restito.server.StubServer
import org.glassfish.grizzly.http.util.HttpStatus.{INTERNAL_SERVER_ERROR_500, MOVED_PERMANENTLY_301, NOT_FOUND_404}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LotteryClientTest extends TestKit(ActorSystem("LotteryClientTest")) with FlatSpecLike with BeforeAndAfterAll
  with BeforeAndAfter with Matchers {

  private val endpoint = "/lottery"

  private var stubServer: StubServer = _

  private var client: LotteryClient = _

  it should "fetch lottery page" in {
    // given
    whenHttp(stubServer)
      .`match`(Condition.get(endpoint))
      .`then`(resourceContent(getClass.getResource("/lotto-default-prize.html")))

    // when
    val result = client.fetchRawPage()

    // then
    Await.result(result, 10 seconds).length should be > 0
  }

  it should "propagate Http 404" in {
    // given
    whenHttp(stubServer)
      .`match`(Condition.get(endpoint))
      .`then`(status(NOT_FOUND_404))

    // when
    val result = client.fetchRawPage()

    // then
    a[PageNotFoundException] should be thrownBy {
      Await.result(result, 10 seconds)
    }
  }

  it should "propagate Http 500" in {
    // given
    whenHttp(stubServer)
      .`match`(Condition.get(endpoint))
      .`then`(status(INTERNAL_SERVER_ERROR_500))

    // when
    val result = client.fetchRawPage()

    // then
    an[InternalServerErrorException] should be thrownBy {
      Await.result(result, 10 seconds)
    }
  }

  it should "propagate unexpected Http status" in {
    // given
    whenHttp(stubServer)
      .`match`(Condition.get(endpoint))
      .`then`(status(MOVED_PERMANENTLY_301))

    // when
    val result = client.fetchRawPage()

    // then
    an[UnexpectedStatusCodeException] should be thrownBy {
      Await.result(result, 10 seconds)
    }
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system, verifySystemShutdown = true)
  }

  before {
    stubServer = new StubServer().run()
    client = new LotteryClient(s"http://localhost:${stubServer.getPort}$endpoint")(system,
      ActorMaterializer()(system), system.dispatcher)
  }

  after {
    stubServer.stop()
  }
}
