package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.service.MessageProviderRegistryTest.TestMessageProvider
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object MessageProviderRegistryTest {

  class TestMessageProvider extends MessageProvider {
    override def name: String = "test"

    override def buildMessage(messageTemplate: String): Future[String] = Future.successful("hello " + messageTemplate)
  }
}

class MessageProviderRegistryTest extends FlatSpec with Matchers {

  private val jobName = "job"
  private val template = "template"

  private val registry = new MessageProviderRegistry(List(new TestMessageProvider))

  it should "use provided template as message if job does not require a provider" in {
    // when
    val message = registry.buildMessage(jobName, template, None)

    // then
    Await.result(message, 1 second) shouldBe template
  }

  it should "use provider to build a message" in {
    // given
    val providerName = "test"

    // when
    val message = registry.buildMessage(jobName, template, Some(providerName))

    // then
    Await.result(message, 1 second) shouldBe s"hello $template"
  }

  it should "fail when there is no provider" in {
    // given
    val providerName = "non existent"

    // when & then
    a[RuntimeException] should be thrownBy {
      Await.result(registry.buildMessage(jobName, template, Some(providerName)), 1 second)
    }
  }
}
