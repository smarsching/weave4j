package org.marsching.weave4j.web

import javax.servlet.http.HttpServletResponse

/**
 * Created by IntelliJ IDEA.
 * User: termi
 * Date: 14.03.2010
 * Time: 14:16:41
 * To change this template use File | Settings | File Templates.
 */

object WeaveErrors {

  private val HeaderWeaveAlert = "X-Weave-Alert";
  
  def errorBadProtocol(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 1)
  }

  def errorOverwriteNotAllowed(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 4)
  }

  def errorUserIdDoesNotMatchAccountInPath(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 5);
  }

  def errorJSONParseFailure(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 6);
  }

  def errorInvalidWBO(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 8);
  }

  def errorUnsupportedFunction(response: HttpServletResponse) {
    errorWeaveBadRequest(response, 11);
  }

  def errorUnsupportedVersion(response: HttpServletResponse) {
    response.addHeader(HeaderWeaveAlert, "The version requested by the client is not supported by this server.")
    errorBadProtocol(response)
  }

  def errorWeaveBadRequest(response: HttpServletResponse, weaveErrorCode: Int) {
    response.setContentType("application/json")
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    response.getWriter.println(weaveErrorCode)
  }

  def errorHttpUnauthorized(response: HttpServletResponse) {
    response.addHeader("WWW-Authenticate", "Basic realm=\"Weave Server\"")
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
  }

  def errorHttpNotFound(response: HttpServletResponse) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND)
  }

  def errorHttpPreConditionFailed(response: HttpServletResponse) {
    response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED)
  }

}