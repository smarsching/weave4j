/* weave4j - Weave Server for Java
 * Copyright (C) 2011  Sebastian Marsching
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

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base32

/**
 * Provides utility functions for handling Weave usernames.
 */
object UsernameHelper {

  /**
   * Encodes a username the same way, the Firefox Sync client encodes an
   * e-mail address to create the username.
   * 
   * @param username username to be encoded
   * @return encoded username
   */
  def encodeUsername(username: String): String = {
    val usernameBytes = username.getBytes("utf-8")
    val md = MessageDigest.getInstance("SHA1")
    val digestBytes = md.digest(usernameBytes)
    val base32 = new Base32
    new String(base32.encode(digestBytes), "utf-8").toLowerCase
  }

}
