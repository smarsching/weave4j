package org.marsching.weave4j.web

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: termi
 * Date: 20.03.2010
 * Time: 16:55:47
 * To change this template use File | Settings | File Templates.
 */

object PasswordHelper {
  private val SaltLength = 4
  private val SSHAPrefix = "{SSHA}"

  protected val logger = LoggerFactory.getLogger(this.getClass)

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