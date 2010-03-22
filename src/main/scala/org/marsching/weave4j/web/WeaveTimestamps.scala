/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2010  Sebastian Marsching
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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