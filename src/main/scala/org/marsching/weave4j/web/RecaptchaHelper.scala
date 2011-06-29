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

import java.net.URL
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Provides utility functions for using the reCAPTCHA service.
 */
object RecaptchaHelper {

  def validateCaptcha(privateKey: String, remoteIpAddress: String, challenge: String, response: String): Boolean = {
    val verifyUrl = new URL("http://www.google.com/recaptcha/api/verify")
    val parameters = Map(
      "privatekey" -> privateKey,
      "remoteip" -> remoteIpAddress,
      "challenge" -> challenge,
      "response" -> response)
    val encodedParameters = parameters.map((keyValue) => {
      val (key, value) = keyValue
      key + "=" + URLEncoder.encode(value, "UTF-8")
    })
    val formData = encodedParameters.mkString("&")

    val connection = verifyUrl.openConnection
    connection.setDoInput(true)
    connection.setDoOutput(true)
    connection.setConnectTimeout(10000)
    connection.setReadTimeout(10000)
    val out = connection.getOutputStream
    out.write(formData.getBytes("UTF-8"))
    out.flush
    out.close
    val in = connection.getInputStream
    val reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))
    val line = reader.readLine
    reader.close
    in.close
    line.trim == "true"
  }
}
