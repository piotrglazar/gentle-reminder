package com.piotrglazar.reminder.service

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.testkit.TestKit
import com.piotrglazar.reminder.config.ReminderConfig.JobConfig
import com.piotrglazar.reminder.service.MessageSink.SinkName
import com.piotrglazar.reminder.service.Worker.Tick
import com.piotrglazar.reminder.service.WorkerTest.CapturingSink
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WorkerTest {

  class CapturingSink(sinkName: String, operation: Option[() => Unit] = None) extends MessageSink {

    val receivedMessage: AtomicReference[Option[String]] = new AtomicReference[Option[String]](None)

    override def name: SinkName = SinkName(sinkName)

    override def sendMessage(message: String): Future[Unit] = Future {
      operation.foreach(_.apply())
      receivedMessage.set(Some(message))
    }
  }
}

class WorkerTest extends TestKit(ActorSystem("WorkerTest")) with FlatSpecLike with BeforeAndAfterAll with BeforeAndAfter
  with MockitoSugar with Eventually with Matchers {

  private var worker: ActorRef = _

  after {
    worker ! PoisonPill
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system, verifySystemShutdown = true)
  }

  it should "forward message to appropriate sink" in {
    // given
    val sink = new CapturingSink("sink")
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "sink", "message"))))

    // when
    worker ! Tick("job")

    // then
    eventually {
      sink.receivedMessage.get() should be a 'defined
      sink.receivedMessage.get().get shouldEqual "message"
    }
  }

  it should "do nothing when there is no such job" in {
    // given
    val sink = new CapturingSink("sink")
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "sink", "message"))))

    // when
    worker ! Tick("unknown job")

    // then nothing
  }

  it should "do nothing when there is no such sink" in {
    // given
    val sink = new CapturingSink("sink")
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "unknown sink", "message"))))

    // when
    worker ! Tick("job")

    // then nothing
  }

  it should "not stall when one sink is slower than the other one" in {
    // given
    val endLatch = new CountDownLatch(1)
    val sinkLatch = new CountDownLatch(1)
    val sink1 = new CapturingSink("sink1", Some(() => sinkLatch.await()))
    val sink2 = new CapturingSink("sink2", Some(() => {
      sinkLatch.countDown()
      endLatch.countDown()
    }))
    worker = system.actorOf(Worker.props(List(sink1, sink2), List(JobConfig("job1", "sink1", "message"),
      JobConfig("job2", "sink2", "message"))))

    // when
    worker ! Tick("job1")
    worker ! Tick("job2")

    // then
    endLatch.await(10, TimeUnit.SECONDS) shouldEqual true
  }
}
