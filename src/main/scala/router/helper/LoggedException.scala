package router.helper

import com.typesafe.scalalogging.Logger

final case class LoggedException(private val message: String = "", private val cause: Throwable = None.orNull, private val evidence: Logged) extends Exception(message, cause)

object LoggedException {
  def getInstanceFuture(evidence: Logged) = scala.concurrent.Future.failed(getInstance(evidence))

  def getInstance(evidence: Logged) = {
    new LoggedException(evidence = evidence)
  }
}