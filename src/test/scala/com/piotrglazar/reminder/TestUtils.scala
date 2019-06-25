package com.piotrglazar.reminder

import scala.io.Source

object TestUtils {

  def readResource(resourceName: String): String =
    Source.fromFile(getClass.getResource(resourceName).toURI, "UTF-8").mkString
}
