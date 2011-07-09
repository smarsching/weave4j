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

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation.RequestMethod
import org.marsching.weave4j.web.TransactionManager
import org.marsching.weave4j.web.UsernameHelper
import org.marsching.weave4j.dbo.WeaveUserDAO
import org.springframework.web.bind.annotation.RequestParam
import org.marsching.weave4j.dbo.exception.InvalidUsernameException
import org.marsching.weave4j.dbo.exception.InvalidPasswordException
import org.springframework.web.bind.annotation.PathVariable
import org.marsching.weave4j.dbo.WeaveStorageDAO
import java.security.MessageDigest
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Base32
import org.springframework.web.servlet.view.RedirectView

/**
 * Handles the HTTP requests for the administative interface.
 */
@Controller
class AdminController {

  /**
   * Transaction manager for managing database transactions.
   */
  protected var transactionManager: TransactionManager = null

  /**
   * DAO for accessing user objects.
   */
  protected var userDAO: WeaveUserDAO = null

  /**
   * DAO for accessing collections and WBOs.
   */
  protected var storageDAO: WeaveStorageDAO = null

  @RequestMapping(value = Array("/"), method = Array(RequestMethod.GET))
  def index(): ModelAndView = {
    transactionManager.withReadOnlyTransaction {
      val users = userDAO.getUsers
      val mav = new ModelAndView("index")
      mav.addObject("users", users)
      mav
    }
  }

  @RequestMapping(value = Array("/adminui"), method = Array(RequestMethod.GET))
  def indexRedirect(): RedirectView = {
    new RedirectView("/adminui/", true, true)
  }

  @RequestMapping(value = Array("/create/user"), method = Array(RequestMethod.POST))
  def createUser(@RequestParam("username") username: String, @RequestParam("email") email: String, @RequestParam("password") password: String): ModelAndView = {
    val useUsername =
      if (username.nonEmpty)
        username
      else
        UsernameHelper.encodeUsername(email)
    try {
      transactionManager.withReadWriteTransaction {
        if (userDAO.findUser(useUsername) != null) {
          val mav = new ModelAndView("createUserFailure")
          mav.addObject("reason", "username_already_in_use")
        } else {
          try {
            userDAO.createUser(useUsername, password, email)
            new ModelAndView("createUserSuccess")
          } catch {
            case e: InvalidUsernameException => {
              val newUsername = UsernameHelper.encodeUsername(useUsername)
              if (userDAO.findUser(newUsername) != null) {
                val mav = new ModelAndView("createUserFailure")
                mav.addObject("reason", "username_already_in_use")
              } else {
                userDAO.createUser(newUsername, password, email)
                new ModelAndView("createUserSuccess")
              }
              
            }
          }
        }
      }
    } catch {
      case e: InvalidUsernameException => {
        val mav = new ModelAndView("createUserFailure")
        mav.addObject("reason", "username")
      }
      case e: InvalidPasswordException => {
        val mav = new ModelAndView("createUserFailure")
        mav.addObject("reason", "password")
      }
      case e: Throwable => {
        val mav = new ModelAndView("createUserFailure")
        mav.addObject("reason", "generic")
        mav.addObject("throwable", e)
      }
    }
  }

  @RequestMapping(value = Array("/user/{username}/deleteForm"), method = Array(RequestMethod.GET))
  def deleteUserForm(@PathVariable("username") username: String): ModelAndView = {
    val mav = new ModelAndView("deleteUserForm")
    mav.addObject("username", username)
  }

  @RequestMapping(value = Array("/user/{username}/delete"), method = Array(RequestMethod.POST))
  def deleteUser(@PathVariable("username") username: String): ModelAndView = {
    transactionManager.withReadWriteTransaction {
      try {
        val user = userDAO.findUser(username)
        if (user != null) {
          storageDAO.deleteAllCollections(user)
        }
        userDAO.deleteUser(username)
        new ModelAndView("deleteUserSuccess")
      } catch {
        case e: Throwable => {
          val mav = new ModelAndView("deleteUserFailure")
          mav.addObject("throwable", e)
        }
      }
    }
  }

  @RequestMapping(value = Array("/user/{username}/changePasswordForm"), method = Array(RequestMethod.GET))
  def changeUserPasswordForm(@PathVariable("username") username: String): ModelAndView = {
    val mav = new ModelAndView("changeUserPasswordForm")
    mav.addObject("username", username)
  }

  @RequestMapping(value = Array("/user/{username}/changePassword"), method = Array(RequestMethod.POST))
  def changeUserPassword(@PathVariable("username") username: String, @RequestParam("password") password: String): ModelAndView = {
    transactionManager.withReadWriteTransaction {
      try {
        userDAO.updatePassword(username, password)
        new ModelAndView("changeUserPasswordSuccess")
      } catch {
        case e: InvalidPasswordException => {
          val mav = new ModelAndView("changeUserPasswordFailure")
          mav.addObject("reason", "password")
        }
        case e: Throwable => {
          val mav = new ModelAndView("changeUserPasswordFailure")
          mav.addObject("reason", "generic")
          mav.addObject("throwable", e)
        }
      }
    }
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
}
