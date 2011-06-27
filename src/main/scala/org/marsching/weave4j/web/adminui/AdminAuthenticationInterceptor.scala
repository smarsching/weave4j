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
package org.marsching.weave4j.web.adminui

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.codec.binary.Base64
import org.marsching.weave4j.web.PasswordHelper
import org.marsching.weave4j.web.AuthenticationHelper

/**
 * Handler interceptor that requires one specific username / password 
 * combination in an HTTP Basic Auth header.
 */
class AdminAuthenticationInterceptor extends HandlerInterceptorAdapter {
  private var passwordHash = ""
  private var username = ""
  
  override def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): Boolean = {
    AuthenticationHelper.extractAuthenticationInfo(request) match {
      case Some((username, password)) => {
        if (username == this.username && PasswordHelper.validatePasswordSSHA(password, this.passwordHash)) {
          true
        } else {
          responseNotAuthorized(response)
        }
      }
      case None => responseNotAuthorized(response)
    }
  }
  
  /**
   * Sets the username to look for.
   * 
   * @param username the username to expect
   */
  def setUsername(username: String) {
    this.username = username
  }
  
  /**
   * Set the hash of the password to look for.
   * @param passwordHash password hash in the following format:
   *    "{SSHA}<Base64 encoded data>", where the last four bytes of the
   *    Base64 encoded data are a salt and the other bytes are the SHA1 hash
   *    of the concatenation of the password and the salt.
   */
  def setPasswordHash(passwordHash: String) {
    this.passwordHash = passwordHash
  }

  private def responseNotAuthorized(response: HttpServletResponse) = {
    AuthenticationHelper.sendAuthenticationRequired(response, "weave4j Administrator's Panel")
    false
  }
}
