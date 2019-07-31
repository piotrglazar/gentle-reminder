package com.piotrglazar.reminder.service

import java.text.NumberFormat
import java.util.Locale

import org.scalatest.{FlatSpec, Matchers}

class FormatTest extends FlatSpec with Matchers {

  it should "pretty print number" in {
    // given
    val locale = new Locale("pl", "PL")
    val format = NumberFormat.getCurrencyInstance(locale)
    val number = 10000000

    // when
    val prettyNumber = format.format(number)

    // then
    prettyNumber shouldBe "10\u00A0000\u00A0000 zł"
  }
}
