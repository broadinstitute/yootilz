package org.broadinstitute.yootilz.core.snag

class SnagException(val snag: Snag) extends Exception(snag.message) {

}

object SnagException {
  def apply(snag: Snag): SnagException = new SnagException(snag)
}
