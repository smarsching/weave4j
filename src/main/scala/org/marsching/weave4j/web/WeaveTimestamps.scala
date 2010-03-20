package org.marsching.weave4j.web

/**
 * Utility functions for Weave timestamps.
 *
 * @author Sebastian Marsching
 */

object WeaveTimestamps {

  /**
   * Returns the current time in seconds since 01/01/1970.
   *
   * @return current timestamp
   */
  def currentTime: BigDecimal = {
    val systemTimeInCentiSeconds = System.currentTimeMillis / 10
    return BigDecimal(systemTimeInCentiSeconds) / BigDecimal(100)
  }
  
}