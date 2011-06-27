/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2010-2011  Sebastian Marsching
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

import javax.servlet.http.HttpServletResponse

/**
 * Utility class for handling errors.
 *
 * @author Sebastian Marsching
 */
object WeaveErrors {

  private val HeaderWeaveAlert = "X-Weave-Alert";

  /**
   * Notifies the client that a "bad protocol" type error occurred.
   *
   * @param response HTTP response
   */
  def errorBadProtocol(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 1)
  }

  /**
   * Notifies the client that no username was specified or the specified
   * username is invalid.
   *
   * @param response HTTP response
   */
  def errorInvalidOrMissingUsername(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 3)
  }

  /**
   * Notifies the client that an "overwrite not allowed" type error occurred.
   *
   * @param response HTTP response
   */
  def errorOverwriteNotAllowed(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 4)
  }

  /**
   * Notifies the client that the username in the path does not match the username in the authorization header.
   *
   * @param response HTTP response
   */
  def errorUserIdDoesNotMatchAccountInPath(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 5);
  }

  /**
   * Notifies the client that a JSON parse error occurred.
   *
   * @param response HTTP response
   */
  def errorJSONParseFailure(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 6);
  }

  /**
   * Notifies the client that an invalid WBO has been sent.
   *
   * @param response HTTP response
   */
  def errorInvalidWBO(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 8);
  }

  /**
   * Notifies the client that the requested password is not strong enough.
   *
   * @param response HTTP response
   */
  def errorRequestedPasswordNotStrongEnough(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 9)
  }

  /**
   * Notifies the client that the requested function is not supported by the server.
   *
   * @param response HTTP response
   */

  def errorUnsupportedFunction(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 11);
  }

  /**
   * Notifies the client that the requested protocol version is not supported by the server.
   *
   * @param response HTTP response
   */
  def errorUnsupportedVersion(response: HttpServletResponse) {
    response.addHeader(HeaderWeaveAlert, "The version requested by the client is not supported by this server.")
    errorBadProtocol(response)
  }

  /**
   * Notifies the client of a Weave specific error using the "bad request" HTTP status code.
   *
   * @param response HTTP response
   * @param weaveErrorCode Weave specific error code sent in the response body
   */
  protected def errorWeaveBadRequest(response: HttpServletResponse, weaveErrorCode: Int) {
    response.setContentType("application/json")
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    response.getWriter.println(weaveErrorCode)
  }

  /**
   * Notifies the client that authentication is required in order t access the requested resource.
   *
   * @param response HTTP response
   */
  def errorHttpUnauthorized(response: HttpServletResponse) {
    response.addHeader("WWW-Authenticate", "Basic realm=\"Weave Server\"")
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
  }

  /**
   * Notifies the client that the requested resource does not exist.
   *
   * @param response HTTP response
   */
  def errorHttpNotFound(response: HttpServletResponse) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND)
  }

  /**
   * Notifies the client that a precondition for this request has not been satisfied.
   *
   * @param response HTTP response
   */
  def errorHttpPreConditionFailed(response: HttpServletResponse) {
    response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED)
  }

}