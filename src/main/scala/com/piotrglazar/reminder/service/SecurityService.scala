package com.piotrglazar.reminder.service

class SecurityService(private val password: String) {

  def passwordMatches(userSuppliedPassword: String): Boolean = {
    password == userSuppliedPassword
  }
}
