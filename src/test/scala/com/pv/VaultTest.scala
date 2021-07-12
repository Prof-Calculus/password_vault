/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv

import com.pv.interaction.IoInterface
import com.pv.tools.crypto.CryptoHelper
import com.pv.tools.crypto.CryptoHelper.EncryptedString
import java.nio.BufferUnderflowException
import javax.crypto.BadPaddingException
import org.junit.Ignore
import org.junit.Test
import org.scalatest.matchers.should._
import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.user.UserInfo
import scala.collection.mutable.ListBuffer


class VaultTest extends Matchers {

  @Test
  def testEncrypt(): Unit = {
    val universe =  MockUniverse.create()

    val message = "string"

    val encrypted: EncryptedString =
      CryptoHelper.encrypt(message, universe.getVaultPassword)

    encrypted should not contain universe.getVaultPassword
    encrypted should not contain message
  }

  @Test
  def testDecrypt(): Unit = {
    val universe =  MockUniverse.create()

    val message = "string"

    val encrypted: EncryptedString =
      CryptoHelper.encrypt(message, universe.getVaultPassword)

    CryptoHelper.decrypt(encrypted, universe.getVaultPassword) shouldBe message

    an [BadPaddingException] should be thrownBy {
      CryptoHelper.decrypt(encrypted, "ARandomPassword")
    }

    an [BufferUnderflowException] should be thrownBy {
      CryptoHelper.decrypt(message, universe.getVaultPassword)
    }
  }

  @Test
  def testUserSpecSerializer(): Unit = {
    val universe = MockUniverse.create()

    val spec = universe.handlers.userHandle.getUserInfo

    val str = spec.toString

    str.length should be > 0

    val newSpec = UserInfo.fromString(spec.toString)

    newSpec.username shouldBe spec.username
    newSpec.dropboxToken shouldBe spec.dropboxToken

  }

  @Test
  def testEditCredentials(): Unit = {
    val universe = MockUniverse.create()

    universe.mockInterface.setNextCredentialToGet()
    universe.vaultManager.addCredential()

    universe.mockInterface.setNextCredentialToGet()
    universe.vaultManager.addCredential()

    universe.mockInterface.setNextCredentialToGet()
    universe.vaultManager.addCredential()

    val original =
      ListBuffer[Credential](universe.handlers.credentials.find(0).get)

    for(i <- Range(1, 10)) {
      val (desc, use, pass) = (s"desc_$i", s"use_$i", s"pass_$i")

      universe.mockInterface.setNextCredentialToChoose(0)
      universe.mockInterface.setNextCredentialToGet(desc, use, pass)

      universe.vaultManager.editCredential()

      val newer = universe.handlers.credentials.find(0).get

      original.foreach {
        c => newer.pastCredentials.find(
          _.isSame(universe.handlers.userHandle.myCryptoHandle)(c)
        ).get.description shouldBe c.description
      }

      newer.getUserEntry(universe.handlers.userHandle.myCryptoHandle) shouldBe
        use
      newer.getPasswordEntry(universe.handlers.userHandle.myCryptoHandle) shouldBe
        pass
      newer.description shouldBe desc

      original.append(newer)
    }

  }


  @Ignore
  @Test
  def testPasswordInput(): Unit = {
    val interface = new IoInterface()

    val pass = interface.getPasswordInput(s"Enter the password")

    interface.putPasswordString(pass)
  }

}
