package org.broadinstitute.yootilz.core.text

object UIStringUtils {

  def prettyTimeInterval(millis: Long): String = {
    if (millis < 1000) {
      s"${millis}ms"
    } else {
      val seconds = millis / 1000
      val millisRemaining = millis % 1000
      if (seconds < 60) {
        s"$seconds.${millisRemaining}s"
      } else {
        val minutes = seconds / 60
        val secondsRemaining = seconds % 60
        if (minutes < 60) {
          s"${minutes}m${secondsRemaining}s"
        } else {
          val hours = minutes / 60
          val minutesRemaining = minutes % 60
          if (hours < 24) {
            s"${hours}h${minutesRemaining}m"
          } else {
            val days = hours / 24
            val hoursRemaining = hours % 24
            if (days < 100) {
              s"${days}d${hoursRemaining}h"
            } else {
              s"${days}d"
            }
          }
        }
      }
    }
  }

  def timeEstimate(timePassed: Long, done: Long, total: Long): Long =
    timeEstimate(timePassed, done.toDouble, total.toDouble)

  def timeEstimate(timePassed: Long, done: Double, total: Double): Long = {
    if(done >= total || done == 0.0) {
      0L
    } else {
      val remaining = total - done
      (remaining*(timePassed/done)).toLong
    }
  }

}
