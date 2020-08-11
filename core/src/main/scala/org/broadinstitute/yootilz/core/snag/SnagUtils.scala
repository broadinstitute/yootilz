package org.broadinstitute.yootilz.core.snag

object SnagUtils {
  def assertNotSnag[T](snagOrValue: Either[Snag, T]): T = {
    snagOrValue match {
      case Left(snag) => throw new SnagException(snag)
      case Right(value) => value
    }
  }
}
