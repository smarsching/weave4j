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

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.commons.codec.binary.Base64
import org.codehaus.jackson.map.JsonMappingException
import org.codehaus.jackson.node.{ArrayNode, ObjectNode, TextNode}
import org.codehaus.jackson.{JsonNode, JsonParseException, JsonProcessingException}
import org.hibernate.HibernateException
import org.marsching.weave4j.dbo.WeaveStorageDAO.SortOrder
import org.marsching.weave4j.dbo.{WeaveBasicObject, WeaveStorageDAO, WeaveUser, WeaveUserDAO}
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.{PlatformTransactionManager, TransactionDefinition}
import org.springframework.web.HttpRequestHandler
import scala.collection.JavaConversions._
import org.codehaus.jackson.node.NullNode
import org.marsching.weave4j.dbo.exception.InvalidUsernameException
import org.marsching.weave4j.dbo.exception.InvalidPasswordException

/**
 * Handler for Weave HTTP requests.
 *
 * @author Sebastian Marsching
 */

class WeaveHttpRequestHandler extends HttpRequestHandler {

  /**
   * Weave response header for current timestamp.
   */
  protected val HeaderTimestamp = "X-Weave-Timestamp"

  /**
   * Weave request header for unmodified since precondition.
   */
  protected val HeaderIfUnmodifiedSince = "X-If-Unmodified-Since"

  /**
   * Weave request header for confirm delete precondition.
   */
  protected val HeaderConfirmDelete = "X-Confirm-Delete"

  /**
   * DAO for accessing user objects.
   */
  protected var userDAO: WeaveUserDAO = null

  /**
   * DAO for accessing collections and WBOs.
   */
  protected var storageDAO: WeaveStorageDAO = null

  /**
   * Transaction manager for managing database transactions.
   */
  protected var transactionManager: TransactionManager = null

  /**
   * Allow new users to register.
   */
  protected var allowUserRegistration: Boolean = true

  /**
   * Logger for this class.
   */
  protected val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Different versions of the Weave protocol.
   */
  object ProtocolVersion extends Enumeration {
    type ProtocolVersion = Value
    val ProtocolVersion_1_0 = Value("1.0")
    val ProtocolVersion_1_1 = Value("1.1")
  }
  import ProtocolVersion._

  /**
   * Main entry method for request handling.
   *
   * @param request HTTP request
   * @param response HTTP response
   */
  override def handleRequest(request: HttpServletRequest, response: HttpServletResponse) {
    val timestamp = WeaveTimestamps.currentTime
    response.addHeader(HeaderTimestamp, timestamp.bigDecimal.toPlainString);

    val pathInfo =
      if (request.getPathInfo == null)
        request.getServletPath
      else
        request.getServletPath + request.getPathInfo
    val PathMatcher = "^(?:/([^/]+))?/(\\d+(?:\\.\\d+)?)(/.+)$".r
    try {
      val PathMatcher(apiName, version, command) = pathInfo
      val protocolVersion = {
        if (version == "1" || version == "1.0") {
          ProtocolVersion_1_0
        } else if (version == "1.1") {
          ProtocolVersion_1_1
        } else {
          logger.debug("Version mismatch: Got version " + version)
          WeaveErrors.errorUnsupportedVersion(response)
          return
        }
      }
      apiName match {
        case null => StorageRequestHandler.handleRequest(request, response, command, timestamp, protocolVersion)
        case "user" => UserRequestHandler.handleRequest(request, response, command, timestamp, protocolVersion)
        case "misc" => MiscRequestHandler.handleRequest(request, response, command, timestamp, protocolVersion)
      }
    } catch {
      case e: MatchError => {
        logger.debug("Invalid path on first match: Received path was " + pathInfo)
        WeaveErrors.errorBadProtocol(response)
      }
      case e: AbortRequestHandlingException => {
        // Do nothing, this exception is only used to be able to end request processing at any place 
      }
    }
  }

  /**
   * Exception that will be caught and ignored by the main request handling method.
   * This exception is used by the request processing code in order to be able to abort
   * the request processing at any point.
   */
  protected class AbortRequestHandlingException extends Exception;

  /**
   * Try to authenticate a user. Will send an authorization required error to the browser, if authentiation fails.
   *
   * @param request HTTP request
   * @param response HTTP response
   * @param pathUsername username from the request path
   * @return the user object representing the authenticated user
   *
   * @throws AbortRequestHandlingException if user cannot be authenticated
   */
  protected def tryLoginUser(request: HttpServletRequest, response: HttpServletResponse, pathUsername: String): WeaveUser = {
    AuthenticationHelper.extractAuthenticationInfo(request) match {
      case Some((username, password)) => {
        val user =
          transactionManager.withReadOnlyTransaction {
            userDAO.findUser(username);
          }
        if (user == null || !PasswordHelper.validatePasswordSSHA(password, user.getPassword())) {
          WeaveErrors.errorHttpUnauthorized(response)
          throw new AbortRequestHandlingException
        }
        if (!username.equalsIgnoreCase(pathUsername)) {
          WeaveErrors.errorUserIdDoesNotMatchAccountInPath(response)
          throw new AbortRequestHandlingException
        }
        user
      }
      case None => {
        WeaveErrors.errorHttpUnauthorized(response)
        throw new AbortRequestHandlingException
      }
    }
  }

  private def readRequestBody(request: HttpServletRequest): String = {
    var charArray: Array[Char] = new Array(1024)
    var charsRead = 0
    val sb = new StringBuilder
    val reader = request.getReader
    do {
      charsRead = reader.read(charArray, 0, 1024)
      if (charsRead > 0)
        sb.appendAll(charArray, 0, charsRead)
    } while (charsRead >= 0)
    sb.toString
  }
  /**
   * Sets the DAO used to access user objects.
   *
   * @param userDAO user DAO
   */
  def setUserDAO(userDAO: WeaveUserDAO) = {
    this.userDAO = userDAO;
  }

  /**
   * Sets the DAO used to access collections and WBOs.
   *
   * @param storageDAO storage DAO
   */
  def setStorageDAO(storageDAO: WeaveStorageDAO) = {
    this.storageDAO = storageDAO;
  }

  /**
   * Sets the transaction manager, used to manage transactions.
   *
   * @param transactionManager transaction manager
   */
  def setTransactionManager(transactionManager: TransactionManager) = {
    this.transactionManager = transactionManager
  }

  /**
   * Enables or disables the automatic user registration.
   * 
   * @param allowUserRegistration if <code>true</code>, new users
   *    can register, if <code>false</code>, new users can only
   *    be created using the administrative interface.
   * 
   */
  def setAllowUserRegistration(allowUserRegistration: Boolean) {
    this.allowUserRegistration = allowUserRegistration
  }

  /**
   * Handles storage HTTP requests.
   */
  private object StorageRequestHandler {

    def handleRequest(request: HttpServletRequest, response: HttpServletResponse, path: String, timestamp: BigDecimal, version: ProtocolVersion) {
      val PathMatcher = "^/([^/]+)/([^/]+)(?:/(.*))?$".r
      try {
        val PathMatcher(username, command, commandInfo) = path;
        command match {
          case "info" => handleInfoCommand(request, response, username, commandInfo, timestamp, version)
          case "storage" => handleStorageCommand(request, response, username, commandInfo, timestamp, version)
        }
      } catch {
        case e: MatchError => WeaveErrors.errorBadProtocol(response)
      }
    }

    def handleInfoCommand(request: HttpServletRequest, response: HttpServletResponse, username: String, path: String, timestamp: BigDecimal, version: ProtocolVersion) {
      transactionManager.withReadOnlyTransaction {
        val timestampInt = timestamp.toBigInt.bigInteger
        val user = tryLoginUser(request, response, username)
        if (request.getMethod() != "GET") {
          WeaveErrors.errorBadProtocol(response)
          return
        }

        path match {
          case "collections" => {
            val collections = user.getCollections()
            val map = JSONHelper.createJSONObjectNode
            for (collection <- user.getCollections()) {
              val typeName = collection.getType()
              val lastModified = storageDAO.getLastModified(user, typeName, timestampInt)
              map.put(typeName, lastModified)
            }
            JSONHelper.writeJSON(request, response, map)
          }

          case "collection_usage" => {
            if (version != ProtocolVersion_1_1) {
              WeaveErrors.errorBadProtocol(response)
            }
            val collections = user.getCollections()
            val map = JSONHelper.createJSONObjectNode
            for (collection <- user.getCollections()) {
              val typeName = collection.getType()
              val size = storageDAO.getCollectionSize(user, typeName, timestampInt)
              map.put(typeName, size)
            }
            JSONHelper.writeJSON(request, response, map)
          }

          case "collection_counts" => {
            val collections = user.getCollections()
            val map = JSONHelper.createJSONObjectNode
            for (collection <- user.getCollections()) {
              val typeName = collection.getType()
              val count = storageDAO.getWBOCount(user, typeName, timestampInt)
              map.put(typeName, count)
            }
            JSONHelper.writeJSON(request, response, map)
          }

          case "quota" => {
            val size = storageDAO.getTotalSize(user, timestampInt)
            val array = JSONHelper.createJSONArrayNode
            array.add(size)
            // We do not have support for quotas yet.
            array.add(NullNode.instance)
            JSONHelper.writeJSON(request, response, array)
          }
        }
      }
    }

    def handleStorageCommand(request: HttpServletRequest, response: HttpServletResponse, username: String, path: String, timestamp: BigDecimal, version: ProtocolVersion) {
      val PathMatcher = "^([^/]+)?(?:/(.*))?$".r
      val PathMatcher(collectionName, wboId) = path
      val timestampInt = timestamp.toBigInt.bigInteger
      val headerIfUnmodifiedSince = request.getHeader(HeaderIfUnmodifiedSince)
      val ifUnmodifiedSince =
        if (headerIfUnmodifiedSince != null)
          BigDecimal(headerIfUnmodifiedSince)
        else
          null

      request.getMethod() match {
        case "GET" => {
          transactionManager.withReadOnlyTransaction {
            val user = tryLoginUser(request, response, username)

            val includeTtl = version == ProtocolVersion_1_1
            if (collectionName == null) {
              WeaveErrors.errorBadProtocol(response)
              return
            }
            if (wboId == null) {
              val ids = {
                val param = request.getParameter("ids")
                if (param == null)
                  null
                else
                  param.split(",").map((s: String) => s.trim).filter((s: String) => s.length > 0)
              }
              val predecessorId = request.getParameter("predecessorid")
              val parentId = request.getParameter("parentid")
              val modifiedBefore = {
                val param = request.getParameter("older")
                if (param != null) {
                  BigDecimal(param)
                } else {
                  null
                }
              }
              val modifiedSince = {
                val param = request.getParameter("newer")
                if (param != null) {
                  BigDecimal(param)
                } else {
                  null
                }
              }
              val full = (request.getParameter("full") != null)

              def getIntParameter(paramName: String): java.lang.Integer = {
                val param = request.getParameter(paramName)
                if (param != null)
                  param.toInt
                else
                  null
              }

              val indexAbove = getIntParameter("index_above")
              val indexBelow = getIntParameter("index_below")
              val limit = getIntParameter("limit")
              val offset = getIntParameter("offset")

              val sortOrder: SortOrder = {
                val param = request.getParameter("sort")
                param match {
                  case "oldest" => SortOrder.OLDEST
                  case "newest" => SortOrder.NEWEST
                  case "index" => SortOrder.INDEX
                  case _ => null
                }
              }

              val idsList = {
                if (ids == null) {
                  null
                } else {
                  java.util.Arrays.asList(ids: _*)
                }
              }
              val modifiedSinceBigDecimal = {
                if (modifiedSince == null) {
                  null
                } else {
                  modifiedSince.bigDecimal
                }
              }
              val modifiedBeforeBigDecimal = {
                if (modifiedBefore == null) {
                  null
                } else {
                  modifiedBefore.bigDecimal
                }
              }

              val wbos = storageDAO.getWBOsFromCollection(user, collectionName, idsList, predecessorId, parentId, modifiedBeforeBigDecimal, modifiedSinceBigDecimal, indexAbove, indexBelow, limit, offset, sortOrder, timestampInt)
              if (wbos.size() == 0) {
                WeaveErrors.errorHttpNotFound(response)
                return
              }
              val array = JSONHelper.createJSONArrayNode
              for (wbo: WeaveBasicObject <- wbos) {
                if (full) {
                  array.add(JSONHelper.weaveBasicObjectToJSON(wbo, includeTtl, timestamp))
                } else {
                  array.add(wbo.getId())
                }
              }
              JSONHelper.writeJSON(request, response, array)
            } else {
              val wbo = storageDAO.getWBO(user, collectionName, wboId, timestampInt)
              if (wbo == null) {
                WeaveErrors.errorHttpNotFound(response)
              } else {
                JSONHelper.writeJSON(request, response, JSONHelper.weaveBasicObjectToJSON(wbo, includeTtl, timestamp))
              }
            }
          }
        }

        case "DELETE" => {
          transactionManager.withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            // Clean-up expired WBOs
            storageDAO.cleanUpExpiredWBOs(timestampInt)

            if (collectionName == null && wboId == null) {
              if (request.getHeader(HeaderConfirmDelete) == null) {
                WeaveErrors.errorHttpPreConditionFailed(response)
                return
              }
              storageDAO.deleteAllCollections(user)
              JSONHelper.writeJSON(request, response, timestamp)
            } else if (collectionName == null && wboId != null) {
              WeaveErrors.errorBadProtocol(response)
            } else if (collectionName != null && wboId == null) {
              val ids = {
                val param = request.getParameter("ids")
                if (param == null)
                  null
                else
                  param.split(",").map((s: String) => s.trim).filter((s: String) => s.length > 0)
              }
              val parentId = request.getParameter("parentid")
              val modifiedBefore = {
                val param = request.getParameter("older")
                if (param != null) {
                  BigDecimal(param)
                } else {
                  null
                }
              }
              val modifiedSince = {
                val param = request.getParameter("newer")
                if (param != null) {
                  BigDecimal(param)
                } else {
                  null
                }
              }

              def getIntParameter(paramName: String): java.lang.Integer = {
                val param = request.getParameter(paramName)
                if (param != null)
                  param.toInt
                else
                  null
              }

              val limit = getIntParameter("limit")
              val offset = getIntParameter("offset")

              val sortOrder: SortOrder = {
                val param = request.getParameter("sort")
                param match {
                  case "oldest" => SortOrder.OLDEST
                  case "newest" => SortOrder.NEWEST
                  case "index" => SortOrder.INDEX
                  case _ => null
                }
              }

              if (collectionModifiedSince(user, collectionName, ifUnmodifiedSince, timestampInt)) {
                WeaveErrors.errorHttpPreConditionFailed(response)
                return
              }

              val idsList = {
                if (ids == null) {
                  null
                } else {
                  java.util.Arrays.asList(ids: _*)
                }
              }
              val modifiedSinceBigDecimal = {
                if (modifiedSince == null) {
                  null
                } else {
                  modifiedSince.bigDecimal
                }
              }
              val modifiedBeforeBigDecimal = {
                if (modifiedBefore == null) {
                  null
                } else {
                  modifiedBefore.bigDecimal
                }
              }

              val wbos = storageDAO.getWBOsFromCollection(user, collectionName, idsList, null, parentId, modifiedBeforeBigDecimal, modifiedSinceBigDecimal, null, null, limit, offset, sortOrder, timestampInt)
              for (wbo <- wbos) {
                storageDAO.deleteWBO(wbo)
              }
              if (ids == null && parentId == null && modifiedBefore == null && modifiedSince == null && limit == null && offset == null) {
                // If all WBOs are deleted, delete collection as well
                storageDAO.deleteCollection(user, collectionName)
              }

              JSONHelper.writeJSON(request, response, timestamp)
            } else if (collectionName != null && wboId != null) {
              if (collectionModifiedSince(user, collectionName, ifUnmodifiedSince, timestampInt)) {
                WeaveErrors.errorHttpPreConditionFailed(response)
                return
              }

              val wbo = storageDAO.getWBO(user, collectionName, wboId, timestampInt)
              storageDAO.deleteWBO(wbo)

              JSONHelper.writeJSON(request, response, timestamp)
            }
          }
        }

        case "POST" => {
          transactionManager.withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            // Clean-up expired WBOs
            storageDAO.cleanUpExpiredWBOs(timestampInt)

            if (collectionName == null || wboId != null) {
              WeaveErrors.errorBadProtocol(response)
              return
            }

            val jsonIn = {
              try {
                JSONHelper.readJSON(request)
              } catch {
                case e: JsonParseException => {
                  WeaveErrors.errorJSONParseFailure(response)
                  throw new AbortRequestHandlingException
                }
              }
            }
            if (!jsonIn.isArray()) {
              WeaveErrors.errorInvalidWBO(response)
              return
            }

            var successIDs: List[String] = List()
            var failedIDs = Map.empty[String, String]

            for (node: JsonNode <- jsonIn.getElements()) {
              if (isJSONValidWeaveBasicObject(node, version)) {
                try {
                  val requestWbo = new WeaveBasicObject()
                  updateWeaveBasicObjectWithDataFromJSON(requestWbo, node, version, timestamp)
                  val wboId = requestWbo.getId()
                  if (wboId == null) {
                    throw new JsonMappingException("Invalid WBO: Id is missing")
                  }
                  val dbWbo = storageDAO.getWBO(user, collectionName, wboId, timestampInt)
                  if (dbWbo == null && requestWbo.getPayload() != null) {
                    storageDAO.insertWBO(user, collectionName, requestWbo)
                  } else if (dbWbo != null) {
                    updateWeaveBasicObjectWithDataFromJSON(dbWbo, node, version, timestamp)
                  }
                  successIDs = wboId :: successIDs
                } catch {
                  case e: JsonProcessingException => {
                    if (wboId != null) {
                      failedIDs += (wboId -> e.getMessage())
                    }
                  }
                  case e: HibernateException => {
                    if (wboId != null) {
                      failedIDs += (wboId -> ("Database Problem: " + e.getMessage()))
                    }
                  }
                }
              } else {
                // Try to extract WBO ID from JSON
                if (node.isObject()) {
                  val idField = node.get("id")
                  if (idField != null && idField.isTextual() && !idField.isNull()) {
                    failedIDs += (idField.getTextValue() -> "Invalid WBO")
                  }
                }
              }
            }

            val root = JSONHelper.createJSONObjectNode()
            root.put("modified", timestamp.bigDecimal)
            val jsonSuccess = JSONHelper.createJSONArrayNode
            for (id: String <- successIDs) {
              jsonSuccess.add(id)
            }
            root.put("success", jsonSuccess)
            val jsonFailed = JSONHelper.createJSONObjectNode
            for (id <- failedIDs.keys) {
              jsonFailed.put(id, failedIDs(id))
            }
            root.put("failed", jsonFailed)

            JSONHelper.writeJSON(request, response, root)
          }
        }

        case "PUT" => {
          transactionManager.withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            // Clean-up expired WBOs
            storageDAO.cleanUpExpiredWBOs(timestampInt)

            if (collectionName == null || wboId == null) {
              WeaveErrors.errorBadProtocol(response)
              return
            }

            val dbWbo = storageDAO.getWBO(user, collectionName, wboId, timestampInt)
            val update = (dbWbo != null)
            val wbo = {
              if (update) {
                dbWbo
              } else {
                val temp = new WeaveBasicObject()
                temp.setId(wboId)
                temp
              }
            }
            try {
              updateWeaveBasicObjectWithDataFromJSON(wbo, JSONHelper.readJSON(request), version, timestamp)
            } catch {
              case e: JsonMappingException => {
                WeaveErrors.errorInvalidWBO(response)
                return
              }
              case e: JsonParseException => {
                WeaveErrors.errorJSONParseFailure(response)
                return
              }
            }
            if (!update && wbo.getPayload() != null) {
              storageDAO.insertWBO(user, collectionName, wbo)
            }

            JSONHelper.writeJSON(request, response, timestamp)
          }
        }

        case _ => {
          WeaveErrors.errorBadProtocol(response);
        }
      }
    }

    private def collectionModifiedSince(user: WeaveUser, collectionName: String, ifModifiedSince: BigDecimal, timestamp: java.math.BigInteger): Boolean = {
      val lastModified = storageDAO.getLastModified(user, collectionName, timestamp)
      if (lastModified != null && ifModifiedSince != null && ifModifiedSince < new BigDecimal(lastModified)) {
        true
      } else {
        false
      }

    }

    private def isJSONValidWeaveBasicObject(root: JsonNode, version: ProtocolVersion): Boolean = {
      if (!root.isObject()) {
        return false
      }
      val parentId = root.get("parentid")
      if (parentId != null && !parentId.isTextual() && !parentId.isNull()) {
        return false
      }
      val predecessorId = root.get("predecessorid")
      if (predecessorId != null && !predecessorId.isTextual() && !predecessorId.isNull()) {
        return false
      }
      val sortIndex = root.get("sortindex");
      if (sortIndex != null && !sortIndex.isIntegralNumber && !sortIndex.isNull()) {
        return false
      }
      val payload = root.get("payload")
      if (payload != null && !payload.isTextual() && !payload.isNull()) {
        return false
      }
      if (version == ProtocolVersion_1_1) {
        val ttl = root.get("ttl")
        if (ttl != null && (!ttl.isNumber || ttl.getBigIntegerValue.compareTo(java.math.BigInteger.ZERO) == -1))
          return false
      }
      return true
    }

    private def updateWeaveBasicObjectWithDataFromJSON(wbo: WeaveBasicObject, root: JsonNode, version: ProtocolVersion, timestamp: BigDecimal) {
      def invalidWBO(): Nothing = {
        throw new JsonMappingException("Invalid WBO")
      }

      if (!root.isObject()) {
        invalidWBO
      }
      val id = root.get("id")
      if (id != null) {
        if (!id.isTextual() || id.isNull()) {
          invalidWBO
        }
        wbo.setId(id.getTextValue())
      }
      val parentId = root.get("parentid")
      if (parentId != null) {
        if (!parentId.isTextual() && !parentId.isNull()) {
          invalidWBO
        }
        if (parentId.isNull()) {
          wbo.setParentId(null)
        } else {
          wbo.setParentId(parentId.getTextValue())
        }
      }
      val predecessorId = root.get("predecessorid")
      if (predecessorId != null) {
        if (!predecessorId.isTextual() && !predecessorId.isNull()) {
          invalidWBO
        }
        if (predecessorId.isNull()) {
          wbo.setPredecessorId(null)
        } else {
          wbo.setPredecessorId(predecessorId.getTextValue())
        }
      }
      val sortIndex = root.get("sortindex");
      if (sortIndex != null) {
        if (!sortIndex.isIntegralNumber && !sortIndex.isNull()) {
          invalidWBO
        }
        if (sortIndex.isNull()) {
          wbo.setSortIndex(null)
        } else {
          wbo.setSortIndex(sortIndex.getNumberValue().intValue())
        }
      }
      val payload = root.get("payload")
      if (payload != null) {
        if (!payload.isTextual() && !payload.isNull()) {
          invalidWBO
        }
        if (!payload.isNull()) {
          wbo.setPayload(payload.getTextValue())
        }
      }
      val ttl = root.get("ttl")
      if (ttl != null && version == ProtocolVersion_1_1) {
        if (!ttl.isNumber)
          invalidWBO
        val ttlValue = ttl.getBigIntegerValue
        if (ttlValue.compareTo(java.math.BigInteger.ZERO) == -1)
          invalidWBO
        wbo.setTtl(ttlValue.add(timestamp.toBigInt.bigInteger))
      }
      wbo.setModified(timestamp.bigDecimal)
    }

  }

  /**
   * Handles user HTTP requests.
   */
  private object UserRequestHandler {

    def handleRequest(request: HttpServletRequest, response: HttpServletResponse, path: String, timestamp: BigDecimal, version: ProtocolVersion) {
      if (version != ProtocolVersion_1_0) {
        logger.debug("Version mismatch: Got version " + version)
        WeaveErrors.errorUnsupportedVersion(response)
        return
      }

      val PathMatcher = "^/([^/]+)(?:/(.*))?$".r
      val PathMatcher(username, command) = path;

      request.getMethod() match {
        case "DELETE" => {
          transactionManager.withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            if (command != null) {
              WeaveErrors.errorBadProtocol(response)
              return
            }
            storageDAO.deleteAllCollections(user)
            userDAO.deleteUser(user.getUsername)
          }
        }

        case "GET" => {
          transactionManager.withReadOnlyTransaction {
            command match {
              case null => {
                if (userDAO.findUser(username) != null) {
                  JSONHelper.writeJSON(request, response, 1)
                } else {
                  JSONHelper.writeJSON(request, response, 0)
                }
              }

              case "node/weave" => {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND)
                JSONHelper.writeJSON(request, response, new TextNode("No location"))
              }

              case "password_reset" => {
                WeaveErrors.errorUnsupportedFunction(response)
              }

              case _ => {
                WeaveErrors.errorBadProtocol(response)
              }
            }
          }
        }

        case "POST" => {
          transactionManager.withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            command match {
              case "email" => {
                val eMail = readRequestBody(request).trim
                userDAO.updateEMail(user.getUsername(), eMail)
                JSONHelper.writeJSON(request, response, eMail)
              }

              case "password" => {
                val password = readRequestBody(request).trim
                try {
                  userDAO.updatePassword(user.getUsername(), PasswordHelper.cryptPasswordSSHA(password))
                  JSONHelper.writeJSON(request, response, "success")
                } catch {
                  case e: InvalidPasswordException => {
                    WeaveErrors.errorRequestedPasswordNotStrongEnough(response)
                  }
                }
              }

              case _ => {
                WeaveErrors.errorBadProtocol(response)
              }
            }
          }
        }

        case "PUT" => {
          transactionManager.withReadWriteTransaction {
            if (command != null) {
              WeaveErrors.errorBadProtocol(response)
              return
            }

            if (!allowUserRegistration) {
              WeaveErrors.errorUnsupportedFunction(response)
              return
            }

            try {
              val root = JSONHelper.readJSON(request)
              if (!root.isObject()) {
                throw new JsonMappingException("Invalid user object");
              }
              val password = root.get("password")
              if (password == null || !password.isTextual()) {
                throw new JsonMappingException("Invalid user object");
              }
              val eMail = root.get("email")
              if (eMail == null || !eMail.isTextual()) {
                throw new JsonMappingException("Invalid user object");
              }
              if (userDAO.findUser(username) != null) {
                WeaveErrors.errorOverwriteNotAllowed(response)
                return
              }
              userDAO.createUser(username, PasswordHelper.cryptPasswordSSHA(password.getTextValue()), eMail.getTextValue())
              JSONHelper.writeJSON(request, response, username.toLowerCase)
            } catch {
              case e: JsonProcessingException => {
                WeaveErrors.errorJSONParseFailure(response)
                return
              }
              case e: InvalidUsernameException => {
                WeaveErrors.errorInvalidOrMissingUsername(response)
                return
              }
              case e: InvalidPasswordException => {
                WeaveErrors.errorRequestedPasswordNotStrongEnough(response)
                return
              }
            }
          }
        }

        case _ => {
          WeaveErrors.errorBadProtocol(response)
        }
      }
    }

  }

  /**
   * Handles miscellaneous HTTP requests.
   */
  private object MiscRequestHandler {

    def handleRequest(request: HttpServletRequest, response: HttpServletResponse, path: String, timestamp: BigDecimal, version: ProtocolVersion) {
      if (version != ProtocolVersion_1_0) {
        logger.debug("Version mismatch: Got version " + version)
        WeaveErrors.errorUnsupportedVersion(response)
        return
      }

      path match {
        case "/captcha_html" => {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND)
          response.setContentType("text/html")
          response.getWriter().print("""<body>No captcha required.<input type="hidden" name="recaptcha_challenge_field" id="recaptcha_challenge_field" value="nocaptcha"><input type="hidden" name="recaptcha_response_field" id="recaptcha_response_field" value="nocaptcha"></body>""");
        }

        case _ => {
          WeaveErrors.errorBadProtocol(response)
        }
      }
    }
  }

}
