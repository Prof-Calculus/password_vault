package util.vault.memoized_metadata.credential

import java.util.Date
import org.json4s.Formats
import org.json4s.native.Serialization.read
import org.json4s.native.Serialization.writePretty
import scala.util.Try
import util.crypto.CryptoHelper.EncryptedString
import util.crypto.CryptoHelper.decryptHandleType
import util.enums.Constants.DELETED_STRING
import util.vault.memoized_metadata.MemoizedMetadata
import util.vault.metadata_manager.MetadataManager
import util.vault.metadata_manager.VaultTable

object Credential extends Serializable {

  implicit val formats: Formats = CredentialConfigs.formats

  def loadFromVaultEntry(id: Int, entry: String): Credential =
    Credential.fromString(entry).copy(id = id)

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

  def fromString(str: String): Credential =
    read[Credential](str)

}

case class Credential(
  id: Int,
  description: EncryptedString,
  userEntry: EncryptedString,
  passwordEntry: EncryptedString,
  pastCredentials: CredentialConfigs,
  passwordTimestamp: Date = new Date(),
) extends MemoizedMetadata {

  implicit val formats: Formats = Credential.formats

  def isSame(decryptor: decryptHandleType)(obj: Credential): Boolean = {
    Try{
      description == obj.description &&
        decryptor(userEntry).equals(decryptor(obj.userEntry)) &&
        decryptor(passwordEntry).equals(decryptor(obj.passwordEntry))
    }.getOrElse(false)
  }

  override def toString: String = writePretty[Credential](this)

  override def forceSyncDownToVault(metadataManager: MetadataManager): Unit = {
    metadataManager.setEntryByIndex(VaultTable)(id, this.toString)
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

  def getUserEntry(decryptor: decryptHandleType): String =
    decryptor(userEntry)

  def getPasswordEntry(decryptor: decryptHandleType): String =
    decryptor(passwordEntry)

  def getDescription: String = description

  def addCredentialToHistory(decryptor: decryptHandleType)(
    cred: Credential
  ): Credential = {
    // First just combine the past credentials on both credentials. Then
    // insert the input-credential as an expired cred
    pastCredentials.merge(decryptor)(cred.pastCredentials)
    pastCredentials.addOrReplace(decryptor)(cred.getAsExpiredCredential)
    this
  }

  def getMergedIntoLatest(decryptor: decryptHandleType)(
    cred: Credential
  ): Credential = {
    if(passwordTimestamp.equals(cred.passwordTimestamp) ||
      passwordTimestamp.after(cred.passwordTimestamp)) {
      addCredentialToHistory(decryptor)(cred)
    } else {
      cred.getMergedIntoLatest(decryptor)(cred = this)
    }
  }

}
