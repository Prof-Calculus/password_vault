/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.crypto

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64

object CryptoHelper {

  type EncryptedString = String

  type decryptHandleType = (EncryptedString => String)
  type encryptHandleType = (EncryptedString => String)

  type TransformedString = String

  private final val Algorithm = "AES/CBC/PKCS5Padding"
  private final val Utf8 = "UTF-8"
  private final val IvLengthInBytes = 16

  private val Salt: String = "AngelinaJolieActedInAMovieCalledSalt"

  private def randomByteArray(length: Int): Array[Byte] = {
    val bytes = new Array[Byte](length)
    new SecureRandom().nextBytes(bytes)
    bytes
  }

  private def transformPassword(key: String): SecretKey = {
    new SecretKeySpec(
      MessageDigest
        .getInstance("MD5")
        .digest((Salt + key).getBytes(Utf8)),
      "AES")
  }

  def encrypt(
    input: String,
    vaultPassword: TransformedString,
    useRandomIv: Boolean = true
  ): EncryptedString = {

    val vaultKey = transformPassword(vaultPassword)

    val cipher: Cipher = Cipher.getInstance(Algorithm)
    val iv = new IvParameterSpec(
      if (useRandomIv)
        randomByteArray(IvLengthInBytes)
      else
        Array.fill[Byte](IvLengthInBytes)(0)
      )

    cipher.init(Cipher.ENCRYPT_MODE, vaultKey, iv)
    val encrypted = cipher.doFinal(input.getBytes)

    val cipherData = ByteBuffer.allocate(IvLengthInBytes + encrypted.length)
    cipherData.put(iv.getIV)
    cipherData.put(encrypted)

    Base64.encodeBase64String(cipherData.array())
  }

  def decrypt(
    input: EncryptedString,
    vaultPassword: TransformedString
  ): String = {

    val vaultKey = transformPassword(vaultPassword)

    val cipher: Cipher = Cipher.getInstance(Algorithm)
    val decodedBytes = ByteBuffer.wrap(Base64.decodeBase64(input.getBytes))

    val iv = new Array[Byte](IvLengthInBytes)
    decodedBytes.get(iv)
    val encrypted = new Array[Byte](decodedBytes.remaining)
    decodedBytes.get(encrypted)

    cipher.init(Cipher.DECRYPT_MODE, vaultKey, new IvParameterSpec(iv))
    new String(cipher.doFinal(encrypted))
  }

  def definitiveTransformation(input: String): String = {
    encrypt(
      input = input,
      vaultPassword = input,
      useRandomIv = false
    )
  }
}
