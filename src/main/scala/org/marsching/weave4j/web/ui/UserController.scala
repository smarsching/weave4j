/* weave4j - Weave Server for Java
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

package org.marsching.weave4j.web.ui

import org.springframework.stereotype.Controller
import org.marsching.weave4j.dbo.WeaveUserDAO
import org.marsching.weave4j.dbo.WeaveStorageDAO
import org.marsching.weave4j.web.PasswordHelper
import org.marsching.weave4j.web.TransactionManager
import org.marsching.weave4j.web.UsernameHelper
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.bind.annotation.RequestParam
import org.marsching.weave4j.web.CaptchaSettings
import javax.servlet.http.HttpServletRequest
import org.marsching.weave4j.web.RecaptchaHelper
import scala.collection.JavaConversions._
import scala.util.Random
import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import org.marsching.weave4j.dbo.WeaveUser
import org.springframework.mail.MailSender
import org.apache.velocity.app.VelocityEngine
import org.springframework.mail.SimpleMailMessage
import org.apache.velocity.VelocityContext
import java.io.StringWriter
import org.marsching.weave4j.dbo.exception.InvalidPasswordException
import org.apache.commons.codec.binary.Base32


/**
 * Handles the HTTP requests for the user interface.
 */
@Controller
class UserController {

  /**
   * Captcha settings.
   */
  protected var captchaSettings: CaptchaSettings = null

  /**
   * Mail sender used to send password reset code.
   */
  protected var mailSender: MailSender = null

  /**
   * Velocity template engine used to generate password reset code e-mail.
   */
  protected var mailVelocityEngine: VelocityEngine = null

  /**
   * Template message containing headers for the password reset code e-mail.
   */
  protected var passwordResetTemplateMessage: SimpleMailMessage = null

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
  
  /**
   * Key used for signing password reset requests.
   */
  protected val passwordResetKey = generateRandomString(128)

  @RequestMapping(value = Array("/"), method = Array(RequestMethod.GET))
  def index(): ModelAndView = {
    new ModelAndView("index")
  }

  @RequestMapping(value = Array("/ui"), method = Array(RequestMethod.GET))
  def indexRedirect(): RedirectView = {
    new RedirectView("/ui/", true, true)
  }

  @RequestMapping(value = Array("/resetPassword"), method = Array(RequestMethod.GET))
  def resetPasswordStart(): ModelAndView = {
    val mav = new ModelAndView("resetPasswordStart")
    mav.addObject("captchasEnabled", captchaSettings.isEnableCaptchas)
    mav.addObject("recaptchaPublicKey", captchaSettings.getRecaptchaPublicKey)
    mav
  }

  @RequestMapping(value = Array("/resetPasswordStep1"), method = Array(RequestMethod.POST))
  def resetPasswordStep1(@RequestParam("username") username: String, @RequestParam(value = "recaptcha_challenge_field", required = false) recaptchaChallenge: String, @RequestParam(value = "recaptcha_response_field", required = false) recaptchaResponse: String, req: HttpServletRequest): ModelAndView = {
    val user =
      transactionManager.withReadOnlyTransaction {
        val firstAttempt = userDAO.findUser(username)
        if (firstAttempt != null) {
          firstAttempt
        } else {
          userDAO.findUser(UsernameHelper.encodeUsername(username))
        }
      }
    if (captchaSettings.isEnableCaptchas && (recaptchaChallenge == null || recaptchaResponse == null || !RecaptchaHelper.validateCaptcha(captchaSettings.getRecaptchaPrivateKey, req.getRemoteAddr, recaptchaChallenge, recaptchaResponse))) {
      val mav = new ModelAndView("resetPasswordFailure")
      mav.addObject("reason", "captcha")
    } else if (user == null) {
      val mav = new ModelAndView("resetPasswordFailure")
      mav.addObject("reason", "username")
    } else {
      try {
        val mav = new ModelAndView("resetPasswordStep1")
        val (timestamp, challenge) = sendPasswordResetEMail(user)
        mav.addObject("username", user.getUsername)
        mav.addObject("timestamp", timestamp)
        mav.addObject("challenge", challenge)
      } catch {
        case _ => {
          val mav = new ModelAndView("resetPasswordFailure")
          mav.addObject("reason", "sendMail")
        }
      }
    }
  }

  @RequestMapping(value = Array("/resetPasswordStep2"), method = Array(RequestMethod.POST))
  def resetPasswordStep2(@RequestParam("username") username: String, @RequestParam("timestamp") timestamp: Long, @RequestParam("challenge") challenge: String, @RequestParam("password_reset_code") passwordResetCode: String): ModelAndView = {
    val user =
      transactionManager.withReadOnlyTransaction {
        userDAO.findUser(username)
      }
    if (user == null) {
      val mav = new ModelAndView("resetPasswordFailure")
      mav.addObject("reason", "username")
    } else if (!verifyPasswordResetCode(passwordResetCode, user, timestamp, challenge)) {
      val mav = new ModelAndView("resetPasswordFailure")
      mav.addObject("reason", "passwordResetCode")
    } else {
      val mav = new ModelAndView("resetPasswordStep2")
        mav.addObject("username", user.getUsername)
        mav.addObject("timestamp", timestamp)
        mav.addObject("challenge", challenge)
        mav.addObject("passwordResetCode", passwordResetCode)
    }
  }

  @RequestMapping(value = Array("/resetPasswordStep3"), method = Array(RequestMethod.POST))
  def resetPasswordStep3(@RequestParam("username") username: String, @RequestParam("timestamp") timestamp: Long, @RequestParam("challenge") challenge: String, @RequestParam("password_reset_code") passwordResetCode: String, @RequestParam("password") password: String, @RequestParam("password_repeat") passwordRepeat: String): ModelAndView = {
    transactionManager.withReadWriteTransaction {
      val user = userDAO.findUser(username)
      if (user == null) {
        val mav = new ModelAndView("resetPasswordFailure")
        mav.addObject("reason", "username")
      } else if (!verifyPasswordResetCode(passwordResetCode, user, timestamp, challenge)) {
        val mav = new ModelAndView("resetPasswordFailure")
        mav.addObject("reason", "passwordResetCode")
      } else if (password.trim != passwordRepeat.trim) {
        val mav = new ModelAndView("resetPasswordFailure")
        mav.addObject("reason", "passwordMismatch")
      } else {
        try {
          userDAO.updatePassword(user.getUsername, password.trim)
          new ModelAndView("resetPasswordSuccess")
        } catch {
          case e: InvalidPasswordException => {
            val mav = new ModelAndView("resetPasswordFailure")
            mav.addObject("reason", "password")
          }
          case e: Throwable => {
            val mav = new ModelAndView("resetPasswordFailure")
            mav.addObject("reason", "generic")
            mav.addObject("throwable", e)
          }
        }
      }
    }
  }

  @RequestMapping(value = Array("/deleteUser"), method = Array(RequestMethod.GET))
  def deleteUserStart(): ModelAndView = {
    new ModelAndView("deleteUser")
  }

  @RequestMapping(value = Array("/deleteUserConfirm"), method = Array(RequestMethod.POST))
  def deleteUserConfirm(@RequestParam("username") username: String, @RequestParam("password") password: String): ModelAndView = {
    transactionManager.withReadWriteTransaction {
      val user = {
        val userFirstAttempt = userDAO.findUser(username)
        if (userFirstAttempt != null) {
          userFirstAttempt
        } else {
          userDAO.findUser(UsernameHelper.encodeUsername(username))
        }
      }
      if (user == null || !PasswordHelper.validatePasswordSSHA(password, user.getPassword())) {
        val mav = new ModelAndView("deleteUserFailure")
        mav.addObject("reason", "usernameOrPassword")
      } else {
        try {
          storageDAO.deleteAllCollections(user)
          userDAO.deleteUser(user.getUsername)
          new ModelAndView("deleteUserSuccess")
        } catch {
          case e: Throwable => {
            val mav = new ModelAndView("deleteUserFailure")
            mav.addObject("reason", "generic")
            mav.addObject("throwable", e)
          }
        }
      }
    }
  }

  private def sendPasswordResetEMail(user: WeaveUser): (String, String) = {
    val username = user.getUsername
    val oldPasswordHash = user.getPassword
    val challenge = generateRandomString(64)
    val timestamp = System.currentTimeMillis.toString
    val resetCode = generateHash(username + oldPasswordHash + timestamp + challenge + passwordResetKey)

    val template = mailVelocityEngine.getTemplate("passwordResetCode.vm", "utf-8")
    val model = Map("passwordResetCode" -> resetCode)
    val writer = new StringWriter
    template.merge(new VelocityContext(model), writer)
    val mailMessage = new SimpleMailMessage(passwordResetTemplateMessage)
    mailMessage.setTo(user.getEMail)
    mailMessage.setText(writer.getBuffer.toString)
    mailSender.send(mailMessage)

    (timestamp, challenge)
  }

  private def verifyPasswordResetCode(resetCode: String, user: WeaveUser, timestamp: Long, challenge: String): Boolean = {
    val username = user.getUsername
    val oldPasswordHash = user.getPassword
    val expectedResetCode = generateHash(username + oldPasswordHash + timestamp.toString + challenge + passwordResetKey)
    val codeValid = resetCode.trim.toUpperCase == expectedResetCode
    val timestampValid = (System.currentTimeMillis <= (timestamp + 1800000)) && (System.currentTimeMillis >= timestamp)
    codeValid && timestampValid
  }

  private def generateRandomString(length: Int): String = {
    val random = new Random
    val sb = new StringBuilder
    while (sb.length < length) {
      val c = random.nextPrintableChar
      if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
        sb += c
      }
    }
    sb.toString
  }

  private def generateHash(text: String): String = {
    val textBytes = text.getBytes("utf-8")
    val md = MessageDigest.getInstance("SHA1")
    val digestBytes = md.digest(textBytes)
    val base32 = new Base32
    base32.encodeToString(digestBytes).toUpperCase
  }

  /**
   * Sets the captcha settings.
   * 
   * @param captchaSettings object storing captcha settings
   * 
   */
  def setCaptchaSettings(captchaSettings: CaptchaSettings) {
    this.captchaSettings = captchaSettings
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
   * Sets the mail sender used for sending password reset e-mails.
   * 
   * @param mailSender mail sender used to send e-mails
   */
  def setMailSender(mailSender: MailSender) {
    this.mailSender = mailSender
  }

  /**
   * Sets the velocity engine used to render the body of the password reset
   * e-mails.
   * 
   * @param mailVelocityEngine velocity template engine
   */
  def setMailVelocityEngine(mailVelocityEngine: VelocityEngine) {
    this.mailVelocityEngine = mailVelocityEngine
  }

  /**
   * Sets the template message whose headers and settings are used as a 
   * template for the password reset e-mails.
   * 
   * @param passwordResetTemplateMessage template message for password reset
   *    e-mails
   */
  def setPasswordResetTemplateMessage(passwordResetTemplateMessage: SimpleMailMessage) {
    this.passwordResetTemplateMessage = passwordResetTemplateMessage
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