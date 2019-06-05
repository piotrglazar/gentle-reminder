package com.piotrglazar.reminder.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.xebialabs.restito.builder.stub.StubHttp
import com.xebialabs.restito.semantics.{Action, Condition}
import com.xebialabs.restito.server.StubServer
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
    StubHttp.whenHttp(stubServer)
      .`match`(Condition.get(endpoint))
      .`then`(Action.resourceContent(getClass.getResource("/lotto2.html")))

    // when
    val result = client.fetchRawPage()

    // then
    Await.result(result, 1 second).length should be > 0
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system, verifySystemShutdown = true)
  }

  before {
    stubServer = new StubServer().run()
    client = new LotteryClient(s"http://localhost:${stubServer.getPort}$endpoint")(system, ActorMaterializer()(system),
      system.dispatcher)
  }

  after {
    stubServer.stop()
  }
}
