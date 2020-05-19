package com.piotrglazar.reminder.api

object Messages {
  case class SlackMessage(password: String, messageBody: String)
}
