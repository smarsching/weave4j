package org.marsching.weave4j.web

/**
 * Created by IntelliJ IDEA.
 * User: termi
 * Date: 14.03.2010
 * Time: 14:33:20
 * To change this template use File | Settings | File Templates.
 */

object WeaveTimestamps {
  def currentTime: BigDecimal = {
    val systemTimeInCentiSeconds = System.currentTimeMillis / 10
    return BigDecimal(systemTimeInCentiSeconds) / BigDecimal(100)
  }
}