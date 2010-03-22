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

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.codehaus.jackson.{JsonGenerator, JsonEncoding, JsonNode}
import java.io.{OutputStream, ByteArrayOutputStream}
import org.springframework.util.Assert
import collection.jcl.MutableIterator
import org.marsching.weave4j.dbo.WeaveBasicObject
import org.codehaus.jackson.map.{JsonMappingException, ObjectMapper}
import org.codehaus.jackson.node.{TextNode, ObjectNode, ArrayNode}
import org.slf4j.LoggerFactory

/**
 * Utility functions for writing JSON to a HTTP response and reading JSON from HTTP requests.
 *
 * @author Sebastian Marsching 
 */

object JSONHelper {
  /**
   * Content type for JSON
   */
  val TypeApplicationJson = "application/json"

  /**
   * Special "newlines" JSON list content type used by Weave
   */
  val TypeApplicationNewlines = "application/newlines"

  /**
   * Special "whoisi" JSON list content type used by Weave
   */
  val TypeApplicationWhoisi = "application/whoisi"

  /**
   * Logger for this class.
   */
  protected val logger = LoggerFactory.getLogger(this.getClass)

  private val objectMapper = new ObjectMapper()

  /**
   * Creates a root node of an empty JSON tree.
   */
  def createJSONObjectNode(): ObjectNode = {
    objectMapper.createObjectNode()
  }

  /**
   * Creates an empty JSON array.
   */
  def createJSONArrayNode(): ArrayNode = {
    objectMapper.createArrayNode()
  }

  /**
   * Writes a JSON node to a HTTP response.
   *
   * @param request HTTP request
   * @param response HTTP resonse
   * @param node JSON node to be serialized
   */
  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, node: JsonNode) {
    response.setContentType("utf-8")

    if (node.isArray) {
      val acceptHeader = request.getHeader("Accept")
      if (acceptHeader.contains(TypeApplicationWhoisi)) {
        response.setContentType(TypeApplicationWhoisi)
        val baos = new ByteArrayOutputStream()
        val jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(baos, JsonEncoding.UTF8)
        val os = response.getOutputStream()
        for (node: JsonNode <- new MutableIterator.Wrapper(node.getElements())) {
          objectMapper.writeValue(jsonGenerator, node)
          val jsonBytes = baos.toByteArray()
          baos.reset()
          os.write(longTo4Bytes(jsonBytes.length))
          os.write(jsonBytes)
        }
      } else if (acceptHeader.contains(TypeApplicationNewlines)) {
        response.setContentType(TypeApplicationNewlines)
        val baos = new ByteArrayOutputStream()
        val jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(baos, JsonEncoding.UTF8)
        val os = response.getOutputStream()
        for (node: JsonNode <- new MutableIterator.Wrapper(node.getElements())) {
          objectMapper.writeValue(jsonGenerator, node)
          val jsonBytes = baos.toByteArray()
          baos.reset()
          val jsonBytesConverted: Array[Byte] = jsonBytes map ((b: Byte) => {
            if (b == '\n')
              0x0a
            else
              b
          })
          os.write(jsonBytesConverted);
          os.write('\n');
        }
      } else {
        response.setContentType(TypeApplicationJson)
        objectMapper.writeValue(response.getWriter(), node)
      }
    } else {
      response.setContentType(TypeApplicationJson)
      objectMapper.writeValue(response.getWriter(), node)
    }
  }

  /**
   * Writes a big decimal to a HTTP response as JSON.
   *
   * @param request HTTP request
   * @param response HTTP resonse
   * @param decimal decimal to be serialized
   */
  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, decimal: BigDecimal) {
    response.setContentType(TypeApplicationJson)
    response.setCharacterEncoding("utf-8")
    response.getWriter().print(decimal.bigDecimal.toPlainString())
  }

  /**
   * Writes an integer to a HTTP response as JSON.
   *
   * @param request HTTP request
   * @param response HTTP resonse
   * @param number integer to be serialized
   */
  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, number: Int) {
    response.setContentType(TypeApplicationJson)
    response.setCharacterEncoding("utf-8")
    response.getWriter().print(number)
  }

  /**
   * Writes a string to a HTTP response as JSON.
   *
   * @param request HTTP request
   * @param response HTTP resonse
   * @param str string to be serialized
   */

  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, str: String) {
    writeJSON(request, response, new TextNode(str))
  }

  /**
   * Reads JSON from the body of an HTTP request.
   *
   * @param request HTTP request
   * @return JSON node representing the body of the request
   * @throws org.codehaus.jackson.JacksonProcessingException if an error occurs while deserializing the JSON  
   */
  def readJSON(request: HttpServletRequest): JsonNode = {
    return objectMapper.readTree(request.getInputStream())
  }

  /**
   * Converts the last 4 bytes of a long to an array of bytes.
   *
   * @param longValue number to be converted
   * @return array with exactly 4 bytes
   */
  protected def longTo4Bytes(longValue: Long): Array[Byte] = {
      Assert.isTrue(longValue < 4294967296L, "Long value has to be smaller than 4294967296 to be stored in 4 bytes.")
      val bytes = new Array[Byte](4)
      bytes(0) = ((longValue >> 24) & 0xff).toByte
      bytes(1) = ((longValue >> 16) & 0xff).toByte
      bytes(2) = ((longValue >> 8) & 0xff).toByte
      bytes(3) = (longValue & 0xff).toByte
      return bytes
  }

  /**
   * Creates a JSON tree from a WBO.
   *
   * @param wbo Weave Basic Object to be serialized
   */
  def weaveBasicObjectToJSON(wbo: WeaveBasicObject): JsonNode = {
    val root = createJSONObjectNode
    root.put("id", wbo.getId())
    val parentId = wbo.getParentId()
    if (parentId != null)
      root.put("parentid", parentId)
    val predecessorId = wbo.getPredecessorId()
    if (predecessorId != null)
      root.put("predecessorid", predecessorId)
    root.put("modified", wbo.getModified())
    val sortIndex = wbo.getSortIndex()
    if (sortIndex != null)
      root.put("sortindex", sortIndex.intValue())
    val payload = wbo.getPayload()
    if (payload != null)
      root.put("payload", payload)
    return root
  }

}