package com.piotrglazar.reminder.service

import com.piotrglazar.reminder.config.ReminderConfig.UserConfig

class UserService(userConfig: List[UserConfig]) {

  private val users: Map[String, String] = userConfig.map(uc => uc.name -> uc.slackName).toMap

  def getExternalUserName(userName: String): Option[String] = {
    users.get(userName)
  }
}
