package com.piotrglazar.reminder

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import com.piotrglazar.reminder.api.Routing
import com.piotrglazar.reminder.client.LotteryClient
import com.piotrglazar.reminder.config.ReminderConfig
import com.piotrglazar.reminder.config.ReminderConfig.CertConfig
import com.piotrglazar.reminder.service._
import com.typesafe.scalalogging.LazyLogging
import configs.Result.{Failure, Success}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object GentleReminder extends App with LazyLogging {

  def run(fullConfig: ReminderConfig): Unit = {
    implicit val system: ActorSystem = ActorSystem("GentleReminder")
    implicit val context: ExecutionContextExecutor = system.dispatcher

    val slackClient = new SlackClient(fullConfig.slackConfig.token, fullConfig.slackConfig.channelId)

    val loggingSink = new LoggingMessageSink

    val slackMessageSink = new SlackMessageSink(slackClient)

    val routing: Routing = new Routing(slackMessageSink,
      new SecurityService(fullConfig.runConfig.maintenancePassword))

    val userService: UserService = new UserService(fullConfig.users)

    val lotteryClient: LotteryClient = new LotteryClient(fullConfig.businessConfig.lotteryApiUrl)

    val lotteryMessageProvider: LotteryMessageService = new LotteryMessageService(lotteryClient,
      fullConfig.businessConfig.lotteryThreshold)

    val messageProviderRegistry: MessageServiceRegistry = new MessageServiceRegistry(List(lotteryMessageProvider))

    val worker = system.actorOf(Worker.props(List(loggingSink, slackMessageSink), fullConfig.jobs, userService,
      messageProviderRegistry))

    SchedulingService.startScheduling(system, fullConfig.jobs, worker)

    val https: HttpsConnectionContext = setupHttps(fullConfig.certConfig)

    Http().newServerAt(fullConfig.runConfig.host, fullConfig.runConfig.port)
      .enableHttps(https)
      .bind(routing.route)

    logger.info(s"Server is running on port ${fullConfig.runConfig.port}")
  }

  ReminderConfig.read() match {
    case Success(fullConfig) =>
      run(fullConfig)
    case Failure(ce) =>
      val exception = ce.configException
      logger.error("Failed to read config", exception)
  }

  private def setupHttps(certConfig: CertConfig): HttpsConnectionContext = {
    val password: Array[Char] = certConfig.password.toCharArray

    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("gentle-reminder.p12")

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    ConnectionContext.httpsServer(sslContext)
  }
}
