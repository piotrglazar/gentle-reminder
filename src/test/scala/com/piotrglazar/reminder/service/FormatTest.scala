package com.piotrglazar.reminder.service

import java.text.NumberFormat;
import java.util.Locale;
import org.scalatest.FlatSpec

class FormatTest extends FlatSpec{

  it should "pretty print number" in {
    val locale = new Locale("pl", "PL")
    val nf = NumberFormat.getCurrencyInstance(locale)

    val result = nf.format(10000000)

    println(result)
  }
}
