package com.piotrglazar.reminder

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.piotrglazar.reminder.api.Routing
import com.piotrglazar.reminder.client.LotteryClient
import com.piotrglazar.reminder.config.ReminderConfig
import com.piotrglazar.reminder.service._
import com.typesafe.scalalogging.LazyLogging
import configs.Result.{Failure, Success}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object GentleReminder extends App with LazyLogging {

  def run(fullConfig: ReminderConfig): Unit = {
    implicit val system: ActorSystem = ActorSystem("GentleReminder")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val context: ExecutionContextExecutor = system.dispatcher

    val slackClient = new SlackClient(fullConfig.slackConfig.token, fullConfig.slackConfig.channelId)

    val loggingSink = new LoggingMessageSink

    val slackMessageSink = new SlackMessageSink(slackClient)

    val routing: Routing = new Routing(slackMessageSink)

    val userService: UserService = new UserService(fullConfig.users)

    val lotteryClient: LotteryClient = new LotteryClient(fullConfig.businessConfig.lotteryApiUrl)

    val lotteryMessageProvider: LotteryMessageService = new LotteryMessageService(lotteryClient,
      fullConfig.businessConfig.lotteryThreshold)

    val messageProviderRegistry: MessageServiceRegistry = new MessageServiceRegistry(List(lotteryMessageProvider))

    val worker = system.actorOf(Worker.props(List(loggingSink, slackMessageSink), fullConfig.jobs, userService,
      messageProviderRegistry))

    SchedulingService.startScheduling(system, fullConfig.jobs, worker)

    val bindingFuture = Http().bindAndHandle(routing.route, fullConfig.runConfig.host, fullConfig.runConfig.port)

    logger.info(s"Server is running on port ${fullConfig.runConfig.port}")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  ReminderConfig.read() match {
    case Success(fullConfig) =>
      run(fullConfig)
    case Failure(ce) =>
      val exception = ce.configException
      logger.error("Failed to read config", exception)
  }
}
