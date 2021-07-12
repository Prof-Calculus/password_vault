/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata.credential

import java.util.Date
import org.json4s.Formats
import org.json4s.native.Serialization.read
import org.json4s.native.Serialization.writePretty
import scala.util.Try
import com.pv.tools.crypto.CryptoHelper.EncryptedString
import com.pv.util.Constants.DELETED_STRING
import com.pv.common.vault.memoized_metadata.MemoizedMetadata
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.VaultTable
import com.pv.tools.crypto.MyCryptoHandle

object Credential extends Serializable {

  implicit val formats: Formats = CredentialConfigs.formats

  def loadFromVaultEntry(myCrypto: MyCryptoHandle)(
    id: Int,
    entry: String
  ): Credential = Credential.fromString(myCrypto.decrypt(entry)).copy(id = id)

  def getCredential(
    id: Int,
    description: String,
    userEntry: EncryptedString,
    passwordEntry: EncryptedString
  ): Credential =
    Credential(
      id = id,
      description = description,
      userEntry = userEntry,
      passwordEntry = passwordEntry,
      pastCredentials = CredentialConfigs.initializeEmpty()
    )

  def fromString(str: String): Credential = read[Credential](str)

}

case class Credential(
  id: Int,
  description: String,
  userEntry: EncryptedString,
  passwordEntry: EncryptedString,
  pastCredentials: CredentialConfigs,
  passwordTimestamp: Date = new Date(),
) extends MemoizedMetadata {

  implicit val formats: Formats = Credential.formats

  def isSame(myCrypto: MyCryptoHandle)(obj: Credential): Boolean = {
    Try{
      description == obj.description &&
        myCrypto.decrypt(userEntry).equals(myCrypto.decrypt(obj.userEntry)) &&
        myCrypto.decrypt(passwordEntry).equals(
          myCrypto.decrypt(obj.passwordEntry))
    }.getOrElse(false)
  }

  override def toString: String = writePretty[Credential](this)

  override def forceSyncDownToVault(
    metadataManager: MetadataManager,
    myCrypto: MyCryptoHandle
  ): Unit = {
    metadataManager.setEntryByIndex(VaultTable)(
      id, myCrypto.encrypt(this.toString))
  }

  def getAsExpiredCredential: Credential = {
    pastCredentials.clear()
    this.copy(id = -1)
  }

  def delete: Credential = {
    pastCredentials.clear()
    this.copy(
      userEntry = DELETED_STRING,
      passwordEntry = DELETED_STRING,
      passwordTimestamp = new Date())
  }

  def getUserEntry(myCrypto: MyCryptoHandle): String =
    myCrypto.decrypt(userEntry)

  def getPasswordEntry(myCrypto: MyCryptoHandle): String =
    myCrypto.decrypt(passwordEntry)

  def getDescription: String = description

  def addCredentialToHistory(myCrypto: MyCryptoHandle)(
    cred: Credential
  ): Credential = {
    // First just combine the past credentials on both credentials. Then
    // insert the input-credential as an expired cred
    pastCredentials.merge(myCrypto)(cred.pastCredentials)
    pastCredentials.addOrReplace(myCrypto)(cred.getAsExpiredCredential)
    this
  }

  def getMergedIntoLatest(myCrypto: MyCryptoHandle)(
    cred: Credential
  ): Credential = {
    if(passwordTimestamp.equals(cred.passwordTimestamp) ||
      passwordTimestamp.after(cred.passwordTimestamp)) {
      addCredentialToHistory(myCrypto)(cred)
    } else {
      cred.getMergedIntoLatest(myCrypto)(cred = this)
    }
  }

}
