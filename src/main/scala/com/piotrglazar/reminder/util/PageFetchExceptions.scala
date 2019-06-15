package com.piotrglazar.reminder.util

object PageFetchExceptions {
  class InternalServerErrorException(url: String) extends RuntimeException(s"Internal server error happened on $url")

  class PageNotFoundException(url: String) extends RuntimeException(s"Page $url not found")

  class UnexpectedStatusCodeException(httpCode: Int, url: String)
    extends RuntimeException(s"Unexpected http status code $httpCode on $url")
}
