package org.broadinstitute.yootilz.core.snag

import java.io.{PrintWriter, StringWriter}

import org.broadinstitute.yootilz.core.snag.Snag.WrapperSnag

trait Snag {
  def message: String

  def report: String

  def prefix(message: String): WrapperSnag = Snag(message, this)

  def prefix(message: String, report: String): WrapperSnag = Snag(message, report, this)
}

object Snag {

  case class BasicSnag(message: String, report: String) extends Snag

  case class WrapperSnag(messagePrefix: String, reportPrefix: String, cause: Snag) extends Snag {
    override def message: String = messagePrefix + ": " + cause.message

    override def report: String = reportPrefix + "\n" + cause.report
  }

  case class ThrowableSnag(throwable: Throwable) extends Snag {
    override def message: String = throwable.getMessage

    override def report: String = {
      val stringWriter = new StringWriter()
      throwable.printStackTrace(new PrintWriter(stringWriter))
      stringWriter.toString
    }
  }

  def apply(message: String): BasicSnag = BasicSnag(message, message)

  def apply(message: String, report: String): BasicSnag = BasicSnag(message, report)

  def apply(message: String, cause: Snag): WrapperSnag = WrapperSnag(message, message, cause)

  def apply(message: String, report: String, cause: Snag): WrapperSnag = WrapperSnag(message, report, cause)

  def apply(throwable: Throwable): ThrowableSnag = ThrowableSnag(throwable)
}
