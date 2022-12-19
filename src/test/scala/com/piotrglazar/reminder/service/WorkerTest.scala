package com.piotrglazar.reminder.service

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.testkit.TestKit
import com.piotrglazar.reminder.config.ReminderConfig.{JobConfig, UserConfig}
import com.piotrglazar.reminder.service.MessageSink.SinkName
import com.piotrglazar.reminder.service.Worker.Tick
import com.piotrglazar.reminder.service.WorkerTest.{CapturingSink, DummyUserService}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalatest.concurrent.{Eventually, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WorkerTest {

  class CapturingSink(sinkName: String, operation: Option[() => Unit] = None) extends MessageSink {

    val receivedMessage: AtomicReference[Option[String]] = new AtomicReference[Option[String]](None)

    val receivedMessageUsers: AtomicReference[List[String]] = new AtomicReference[List[String]](List.empty)

    override def name: SinkName = SinkName(sinkName)

    override def sendMessage(message: String, users: List[String]): Future[Unit] = Future {
      operation.foreach(_.apply())
      receivedMessage.set(Some(message))
      receivedMessageUsers.set(users)
    }
  }

  object DummyUserService extends UserService(List.empty)
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
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "sink", "message", List.empty)),
      DummyUserService, emptyRegistry))

    // when
    worker ! Tick("job")

    // then
    eventually(PatienceConfiguration.Timeout(Span(10, Seconds)), PatienceConfiguration.Interval(Span(250, Millis))) {
      sink.receivedMessage.get() should be a Symbol("defined")
      sink.receivedMessage.get().get shouldEqual "message"
    }
  }

  it should "do nothing when there is no such job" in {
    // given
    val sink = new CapturingSink("sink")
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "sink", "message", List.empty)),
      DummyUserService, emptyRegistry))

    // when
    worker ! Tick("unknown job")

    // then nothing
  }

  it should "do nothing when there is no such sink" in {
    // given
    val sink = new CapturingSink("sink")
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "unknown sink", "message", List.empty)),
      DummyUserService, emptyRegistry))

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
    worker = system.actorOf(Worker.props(List(sink1, sink2), List(JobConfig("job1", "sink1", "message", List.empty),
      JobConfig("job2", "sink2", "message", List.empty)), DummyUserService, emptyRegistry))

    // when
    worker ! Tick("job1")
    worker ! Tick("job2")

    // then
    endLatch.await(10, TimeUnit.SECONDS) shouldEqual true
  }

  it should "fetch user names to be used in message" in {
    // given
    val sink = new CapturingSink("sink")
    worker = system.actorOf(Worker.props(List(sink), List(JobConfig("job", "sink", "message", List("Alice", "Bob"))),
      // omitting Bob on purpose
      new UserService(List(UserConfig("Alice", "ecila"))), emptyRegistry))

    // when
    worker ! Tick("job")

    // then
    eventually(PatienceConfiguration.Timeout(Span(10, Seconds)), PatienceConfiguration.Interval(Span(250, Millis))) {
      sink.receivedMessageUsers.get() should have size 1
      sink.receivedMessageUsers.get().head shouldEqual "ecila"
    }
  }

  private def emptyRegistry: MessageServiceRegistry = new MessageServiceRegistry(List.empty)
}
