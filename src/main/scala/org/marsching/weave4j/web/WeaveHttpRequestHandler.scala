package org.marsching.weave4j.web

import org.springframework.web.HttpRequestHandler
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.apache.commons.codec.binary.Base64
import scala.collection.jcl.Conversions._
import scala.collection.jcl
import org.marsching.weave4j.dbo.WeaveStorageDAO.SortOrder
import org.marsching.weave4j.dbo.{WeaveBasicObject, WeaveUser, WeaveStorageDAO, WeaveUserDAO}
import org.codehaus.jackson.map.JsonMappingException
import org.hibernate.HibernateException
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.{TransactionDefinition, PlatformTransactionManager}
import org.slf4j.LoggerFactory
import org.codehaus.jackson.node.{TextNode, ArrayNode, ObjectNode}
import org.codehaus.jackson.{JsonParseException, JsonProcessingException, JsonNode}

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
   * DAO fo accessing user objects.
   */
  protected var userDAO: WeaveUserDAO = null

  /**
   * DAO for accessing collections and WBOs.
   */
  protected var storageDAO: WeaveStorageDAO = null

  /**
   * Spring platform transaction manager for managing database transactions.
   */
  protected var platformTransactionManager: PlatformTransactionManager = null

  /**
   * Logger for this class.
   */
  protected val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Main entry method for request handling.
   *
   * @param request HTTP request
   * @param response HTTP response
   */
  override def handleRequest(request: HttpServletRequest, response: HttpServletResponse) {
    val timestamp = WeaveTimestamps.currentTime
    response.addHeader(HeaderTimestamp, timestamp.bigDecimal.toPlainString);

    val pathInfo = request.getPathInfo();
    val PathMatcher = "^(?:/([^/]+))?/(\\d+(?:\\.\\d+)?)(/.+)$".r
    try {
      val PathMatcher(apiName, version, command) = pathInfo
      if (version != "1" && version != "1.0") {
        logger.debug("Version mismatch: Got version " + version)
        WeaveErrors.errorUnsupportedVersion(response)
        return
      }
      apiName match {
        case null => StorageRequestHandler.handleRequest(request, response, command, timestamp)
        case "user" => UserRequestHandler.handleRequest(request, response, command, timestamp)
        case "misc" => MiscRequestHandler.handleRequest(request, response, command, timestamp)
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
    val authHeader = request.getHeader("Authorization")
    if (authHeader == null) {
      WeaveErrors.errorHttpUnauthorized(response)
      throw new AbortRequestHandlingException
    }
    val AuthHeaderMatcher = "^\\s*Basic\\s+([A-Za-z0-9+/]+={0,2})\\s*$".r
    try {
      val AuthHeaderMatcher(base64Encoded) = authHeader
      val base64Decoded = new String(Base64.decodeBase64(base64Encoded), "utf-8")
      val UsernamePasswordMatcher = "^(.*):(.*)$".r
      val UsernamePasswordMatcher(username, password) = base64Decoded
      val user =
        withReadOnlyTransaction {
          userDAO.findUser(username);
        }
      if (user == null) {
        WeaveErrors.errorHttpUnauthorized(response)
        throw new AbortRequestHandlingException
      }
      if (PasswordHelper.validatePasswordSSHA(password, user.getPassword())) {
        if (!username.equalsIgnoreCase(pathUsername)) {
          WeaveErrors.errorUserIdDoesNotMatchAccountInPath(response)
          throw new AbortRequestHandlingException
        } else {
          return user
        }
      } else {
        WeaveErrors.errorHttpUnauthorized(response)
        throw new AbortRequestHandlingException
      }
    } catch {
      case e: MatchError => {
        WeaveErrors.errorHttpUnauthorized(response)
        throw new AbortRequestHandlingException
      }
    }
  }

  /**
   * Performs an action within a read/write database transaction.
   *
   * @param f action to perform within transaction
   * @return result of <code>f</code>
   */
  protected def withReadWriteTransaction[T](f: => T): T = {
    withTransaction(false)(f)
  }

  /**
   * Performs an action within a read-only database transaction.
   *
   * @param f action to perform within transaction
   * @return result of <code>f</code>
   */
  protected def withReadOnlyTransaction[T](f: => T): T = {
    withTransaction(true)(f)
  }

  /**
   * Performs an action within a database transaction.
   *
   * @param readOnly if set to <code>true</code>, a read-only transaction will be started
   * @param f action to perform within transaction
   * @return result of <code>f</code>
   */
  protected def withTransaction[T](readOnly: Boolean)(f: => T): T = {
    val transactionDefinition = new DefaultTransactionDefinition()
    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER)
    transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE)
    transactionDefinition.setReadOnly(readOnly)
    val transactionStatus = platformTransactionManager.getTransaction(transactionDefinition)
    try {
      val result: T = f
      platformTransactionManager.commit(transactionStatus)
      return result
    } finally {
      if (!transactionStatus.isCompleted()) {
        platformTransactionManager.rollback(transactionStatus)
      }
    }
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
   * Sets the Spring platform transaction manager, used to manager transactions.
   *
   * @param platformTransactionManager transaction manager
   */
  def setPlatformTransactionManager(platformTransactionManager: PlatformTransactionManager) = {
    this.platformTransactionManager = platformTransactionManager
  }

  /**
   * Handles storage HTTP requests.
   */
  private object StorageRequestHandler {

    def handleRequest(request: HttpServletRequest, response: HttpServletResponse, path: String, timestamp: BigDecimal) {
      val PathMatcher = "^/([^/]+)/([^/]+)(?:/(.*))?$".r
      try {
        val PathMatcher(username, command, commandInfo) = path;
        command match {
          case "info" => handleInfoCommand(request, response, username, commandInfo, timestamp)
          case "storage" => handleStorageCommand(request, response, username, commandInfo, timestamp)
        }
      } catch {
        case e: MatchError => WeaveErrors.errorBadProtocol(response)
      }
    }

    def handleInfoCommand(request: HttpServletRequest, response: HttpServletResponse, username: String, path: String, timestamp: BigDecimal) {
      withReadOnlyTransaction {
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
              val lastModified = storageDAO.getLastModified(user, typeName)
              map.put(typeName, lastModified)
            }
            JSONHelper.writeJSON(request, response, map)
          }

          case "collections_count" => {
            val collections = user.getCollections()
            val map = JSONHelper.createJSONObjectNode
            for (collection <- user.getCollections()) {
              val typeName = collection.getType()
              val count = storageDAO.getWBOCount(user, typeName)
              map.put(typeName, count)
            }
            JSONHelper.writeJSON(request, response, map)
          }

          case "quota" => {
            WeaveErrors.errorUnsupportedFunction(response)
          }
        }
      }
    }

    def handleStorageCommand(request: HttpServletRequest, response: HttpServletResponse, username: String, path: String, timestamp: BigDecimal) {
      val PathMatcher = "^([^/]+)?(?:/(.*))?$".r
      val PathMatcher(collectionName, wboId) = path
      val headerIfUnmodifiedSince = request.getHeader(HeaderIfUnmodifiedSince)
      val ifUnmodifiedSince =
        if (headerIfUnmodifiedSince != null)
          BigDecimal(headerIfUnmodifiedSince)
        else
          null

      request.getMethod() match {
        case "GET" => {
          withReadOnlyTransaction {
            val user = tryLoginUser(request, response, username)

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

              val wbos = storageDAO.getWBOsFromCollection(user, collectionName, idsList, predecessorId, parentId, modifiedBeforeBigDecimal, modifiedSinceBigDecimal, indexAbove, indexBelow, limit, offset, sortOrder)
              if (wbos.size() == 0) {
                WeaveErrors.errorHttpNotFound(response)
                return
              }
              val array = JSONHelper.createJSONArrayNode
              for (wbo: WeaveBasicObject <- wbos) {
                if (full) {
                  array.add(JSONHelper.weaveBasicObjectToJSON(wbo))
                } else {
                  array.add(wbo.getId())
                }
              }
              JSONHelper.writeJSON(request, response, array)
            } else {
              val wbo = storageDAO.getWBO(user, collectionName, wboId)
              if (wbo == null) {
                WeaveErrors.errorHttpNotFound(response)
              } else {
                JSONHelper.writeJSON(request, response, JSONHelper.weaveBasicObjectToJSON(wbo))
              }
            }
          }
        }

        case "DELETE" => {
          withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

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

              if (collectionModifiedSince(user, collectionName, ifUnmodifiedSince)) {
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

              val wbos = storageDAO.getWBOsFromCollection(user, collectionName, idsList, predecessorId, parentId, modifiedBeforeBigDecimal, modifiedSinceBigDecimal, indexAbove, indexBelow, limit, offset, sortOrder)
              for (wbo <- wbos) {
                storageDAO.deleteWBO(wbo)
              }
              if (ids == null && parentId == null && modifiedBefore == null && modifiedSince == null && limit == null && offset == null) {
                // If all WBOs are deleted, delete collection as well
                storageDAO.deleteCollection(user, collectionName)
              }

              JSONHelper.writeJSON(request, response, timestamp)
            } else if (collectionName != null && wboId != null) {
              if (collectionModifiedSince(user, collectionName, ifUnmodifiedSince)) {
                WeaveErrors.errorHttpPreConditionFailed(response)
                return
              }

              val wbo = storageDAO.getWBO(user, collectionName, wboId)
              storageDAO.deleteWBO(wbo)

              JSONHelper.writeJSON(request, response, timestamp)
            }
          }
        }

        case "POST" => {
          withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

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

            for (node: JsonNode <- new jcl.MutableIterator.Wrapper(jsonIn.getElements())) {
              if (isJSONValidWeaveBasicObject(node)) {
                try {
                  val requestWbo = new WeaveBasicObject()
                  updateWeaveBasicObjectWithDataFromJSON(requestWbo, node, timestamp)
                  val wboId = requestWbo.getId()
                  if (wboId == null) {
                    throw new JsonMappingException("Invalid WBO: Id is missing")
                  }
                  val dbWbo = storageDAO.getWBO(user, collectionName, wboId)
                  if (dbWbo == null && requestWbo.getPayload() != null) {
                    storageDAO.insertWBO(user, collectionName, requestWbo)
                  } else if (dbWbo != null) {
                    updateWeaveBasicObjectWithDataFromJSON(dbWbo, node, timestamp)
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
          withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            if (collectionName == null || wboId == null) {
              WeaveErrors.errorBadProtocol(response)
              return
            }

            val dbWbo = storageDAO.getWBO(user, collectionName, wboId)
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
              updateWeaveBasicObjectWithDataFromJSON(wbo, JSONHelper.readJSON(request), timestamp)
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

    private def collectionModifiedSince(user: WeaveUser, collectionName: String, ifModifiedSince: BigDecimal): Boolean = {
      val lastModified = storageDAO.getLastModified(user, collectionName)
      if (lastModified != null && ifModifiedSince != null && ifModifiedSince < new BigDecimal(lastModified)) {
        true
      } else {
        false
      }

    }

    private def isJSONValidWeaveBasicObject(root: JsonNode): Boolean = {
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
      return true
    }

    private def updateWeaveBasicObjectWithDataFromJSON(wbo: WeaveBasicObject, root: JsonNode, newModified: BigDecimal) {
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
      wbo.setModified(newModified.bigDecimal)
      return wbo
    }


  }

  /**
   * Handles user HTTP requests.
   */
  private object UserRequestHandler {

    def handleRequest(request: HttpServletRequest, response: HttpServletResponse, path: String, timestamp: BigDecimal) {
      val PathMatcher = "^/([^/]+)(?:/(.*))?$".r
      val PathMatcher(username, command) = path;

      request.getMethod() match {
        case "DELETE" => {
          withReadWriteTransaction {
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
          withReadOnlyTransaction {
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
          withReadWriteTransaction {
            val user = tryLoginUser(request, response, username)

            command match {
              case "email" => {
                val json = JSONHelper.readJSON(request)
                if (!json.isTextual()) {
                  WeaveErrors.errorJSONParseFailure(response)
                  return
                }
                val eMail = json.getTextValue()
                userDAO.updateEMail(user.getUsername(), eMail)
                JSONHelper.writeJSON(request, response, eMail)
              }

              case "password" => {
                val json = JSONHelper.readJSON(request)
                if (!json.isTextual()) {
                  WeaveErrors.errorJSONParseFailure(response)
                  return
                }
                val password = json.getTextValue()
                userDAO.updatePassword(user.getUsername(), PasswordHelper.cryptPasswordSSHA(password))
                JSONHelper.writeJSON(request, response, "success")
              }

              case _ => {
                WeaveErrors.errorBadProtocol(response)
              }
            }
          }
        }

        case "PUT" => {
          withReadWriteTransaction {
            if (command != null) {
              WeaveErrors.errorBadProtocol(response)
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

    def handleRequest(request: HttpServletRequest, response: HttpServletResponse, path: String, timestamp: BigDecimal) {
      path match {
        case "/captcha_html" => {
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
