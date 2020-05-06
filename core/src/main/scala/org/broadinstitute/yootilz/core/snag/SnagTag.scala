package org.broadinstitute.yootilz.core.snag

case class SnagTag(name: String, description: String, parents: Set[SnagTag])

object SnagTag {

  def apply(name: String, description: String): SnagTag = SnagTag(name, description, Set.empty)

  val endOfData: SnagTag = SnagTag("End of data", "Reading failed because end of a data stream is reached.")

}
