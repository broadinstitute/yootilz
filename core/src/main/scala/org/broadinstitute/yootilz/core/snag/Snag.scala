package org.broadinstitute.yootilz.core.snag

import java.io.{PrintWriter, StringWriter}

import org.broadinstitute.yootilz.core.snag.Snag.WrapperSnag

trait Snag {
  def message: String

  def report: String

  def prefix(message: String): WrapperSnag = Snag(message, this)

  def prefix(message: String, report: String): WrapperSnag = Snag(message, report, this)

  def tags: Set[SnagTag]
}

object Snag {

  case class BasicSnag(message: String, report: String, tags: Set[SnagTag]) extends Snag

  case class WrapperSnag(messagePrefix: String, reportPrefix: String, cause: Snag, tags: Set[SnagTag]) extends Snag {
    override def message: String = messagePrefix + ": " + cause.message

    override def report: String = reportPrefix + "\n" + cause.report
  }

  case class ThrowableSnag(throwable: Throwable, tags: Set[SnagTag]) extends Snag {
    override def message: String = "Caught " + throwable.getClass.getName + ": " + throwable.getMessage

    override def report: String = {
      val stringWriter = new StringWriter()
      throwable.printStackTrace(new PrintWriter(stringWriter))
      stringWriter.toString
    }
  }

  def apply(message: String, tags: SnagTag*): BasicSnag = BasicSnag(message, message, tags.toSet)

  def apply(message: String, report: String, tags: SnagTag*): BasicSnag = BasicSnag(message, report, tags.toSet)

  def apply(message: String, cause: Snag, tags: SnagTag*): WrapperSnag =
    WrapperSnag(message, message, cause, tags.toSet)

  def apply(message: String, report: String, cause: Snag, tags: SnagTag*): WrapperSnag =
    WrapperSnag(message, report, cause, tags.toSet)

  def apply(throwable: Throwable, tags: SnagTag*): ThrowableSnag = ThrowableSnag(throwable, tags.toSet)
}
