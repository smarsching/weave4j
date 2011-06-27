/*
 * weave4j - Weave Server for Java
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

import javax.servlet.http.HttpServletRequest
import org.apache.commons.codec.binary.Base64
import javax.servlet.http.HttpServletResponse

/**
 * Provides methods for extracting HTTP Basic Auth information from an HTTP 
 * request.
 */
object AuthenticationHelper {
  /**
   * Extracts HTTP Basic Auth information from a HTTP request.
   * 
   * @param request the HTTP request object
   * @return tuple of username and password or <code>None</code> if
   *    no valid HTTP Basic Auth header is present in the request.
   */
  def extractAuthenticationInfo(request: HttpServletRequest): Option[(String, String)] = {
    val authHeader = request.getHeader("Authorization")
    if (authHeader == null) {
      None
    } else {
      val AuthHeaderMatcher = "^\\s*Basic\\s+([A-Za-z0-9+/]+={0,2})\\s*$".r
      try {
        val AuthHeaderMatcher(base64Encoded) = authHeader
        val base64Decoded = new String(Base64.decodeBase64(base64Encoded), "utf-8")
        val UsernamePasswordMatcher = "^(.*):(.*)$".r
        val UsernamePasswordMatcher(username, password) = base64Decoded
        Some((username, password))
      } catch {
        case e: MatchError => {
          None
        }
      }
    }
  }
  
  /**
   * Sends an authentication request to the browser.
   * 
   * @param response the HTTP response object
   * @param realm the realm to send to the browser
   */
  def sendAuthenticationRequired(response: HttpServletResponse, realm: String) {
    response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"")
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
  }
}
