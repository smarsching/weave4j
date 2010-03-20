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
 * Created by IntelliJ IDEA.
 * User: termi
 * Date: 14.03.2010
 * Time: 17:40:11
 * To change this template use File | Settings | File Templates.
 */

object JSONHelper {
  val TypeApplicationJson = "application/json"
  val TypeApplicationNewlines = "application/newlines"
  val TypeApplicationWhoisi = "application/whoisi"

  protected val logger = LoggerFactory.getLogger(this.getClass)

  private val objectMapper = new ObjectMapper()

  def createJSONObjectNode(): ObjectNode = {
    objectMapper.createObjectNode()
  }

  def createJSONArrayNode(): ArrayNode = {
    objectMapper.createArrayNode()
  }

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

  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, decimal: BigDecimal) {
    response.setContentType(TypeApplicationJson)
    response.setCharacterEncoding("utf-8")
    response.getWriter().print(decimal.bigDecimal.toPlainString())
  }

  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, number: Int) {
    response.setContentType(TypeApplicationJson)
    response.setCharacterEncoding("utf-8")
    response.getWriter().print(number)
  }

  def writeJSON(request: HttpServletRequest, response: HttpServletResponse, str: String) {
    writeJSON(request, response, new TextNode(str))
  }

  def readJSON(request: HttpServletRequest): JsonNode = {
    return objectMapper.readTree(request.getInputStream())
  }

  protected def longTo4Bytes(longValue: Long): Array[Byte] = {
      Assert.isTrue(longValue < 4294967296L, "Long value has to be smaller than 4294967296 to be stored in 4 bytes.")
      val bytes = new Array[Byte](4)
      bytes(0) = ((longValue >> 24) & 0xff).toByte
      bytes(1) = ((longValue >> 16) & 0xff).toByte
      bytes(2) = ((longValue >> 8) & 0xff).toByte
      bytes(3) = (longValue & 0xff).toByte
      return bytes
  }

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