package org.marsching.weave4j.web

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import org.slf4j.LoggerFactory
import scala.util.Random

/**
 * Utility functions for encrypting and validating password.
 *
 * @author Sebastian Marsching
 */

object PasswordHelper {
  private val SaltLength = 4
  private val SSHAPrefix = "{SSHA}"

  /**
   * Logger for this class
   */
  protected val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Encrypts a password using a salted SHA1.
   *
   * @param password plain password
   * @return password hash
   */
  def cryptPasswordSSHA(password: String): String = {
    val random = new Random
    val salt = new Array[Byte](SaltLength)
    random.nextBytes(salt)
    return cryptPasswordSSHA(password, salt)
  }

  private def cryptPasswordSSHA(password: String, salt: Array[Byte]): String = {
    val passwordBytes = password.getBytes("utf-8")
    val allBytes = Array.concat(passwordBytes, salt)
    val md = MessageDigest.getInstance("SHA1")
    val digestBytes = md.digest(allBytes)
    return SSHAPrefix + new String(Base64.encodeBase64(Array.concat(digestBytes, salt)), "utf-8")
  }

  /**
   * Compares a plain password with a hash.
   *
   * @param password plain password
   * @param hash password hash
   *
   * @return <code>true</code> if hash of plain password matches specified hash, <code>false</code> otherwise
   */
  def validatePasswordSSHA(password: String, hash: String): Boolean = {
    if (!hash.startsWith(SSHAPrefix)) {
      return false
    }
    val hashBytes = Base64.decodeBase64(hash.substring(SSHAPrefix.length))
    val digestBytes = hashBytes.slice(0, hashBytes.length - SaltLength)
    val saltBytes = hashBytes.slice(hashBytes.length - SaltLength, hashBytes.length)
    val newHash = cryptPasswordSSHA(password, saltBytes)
    return (hash == newHash)
  }
}