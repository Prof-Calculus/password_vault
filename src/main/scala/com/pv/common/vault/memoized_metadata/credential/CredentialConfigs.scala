/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata.credential

import org.json4s.CustomSerializer
import org.json4s.DefaultFormats
import org.json4s.Formats
import org.json4s.JString
import org.json4s.native.Serialization.read
import org.json4s.native.Serialization.writePretty
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import com.pv.tools.crypto.CryptoHelper.decryptHandleType
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.VaultTable

object CredentialConfigs extends Serializable {

  object CredListSerializer extends CustomSerializer[CredentialConfigs](
    _ => (
      {case JString(s) => CredentialConfigs.fromString(s)},
      {case ms: CredentialConfigs => JString(ms.toString)}
    )
  )

  implicit val formats: Formats = DefaultFormats + CredListSerializer

  def fromString(str: String): CredentialConfigs = {
    new CredentialConfigs(read[mutable.ListBuffer[Credential]](str))
  }

  def loadFromVault(metadataManager: MetadataManager): CredentialConfigs = {
    CredentialConfigs(
      metadataManager.getAllEntries(VaultTable).map {
        case (id, entry) => Credential.loadFromVaultEntry(id, entry)
      }.to[ListBuffer]
    )
  }

  def initializeEmpty(): CredentialConfigs =
    CredentialConfigs(ListBuffer.empty[Credential])

}


case class CredentialConfigs(
  credentials: ListBuffer[Credential] = ListBuffer.empty[Credential]
) extends mutable.Iterable[Credential] with Serializable {

  implicit val formats: Formats = CredentialConfigs.formats

  override def toString: String = writePretty(this.credentials)

  def clear(): Unit = credentials.clear()

  override def iterator: Iterator[Credential] = credentials.iterator

  def merge(decryptor: decryptHandleType)(
    newCredentials: CredentialConfigs
  ): Unit = newCredentials.foreach(addOrReplace(decryptor))

  def length: Int = credentials.length

  override def isEmpty: Boolean = credentials.isEmpty

  def getNextCredentialId: Int = length

  def remove(decryptor: decryptHandleType)(
    cred: Credential,
  ): Credential = {
    val toAdd = cred.getMergedIntoLatest(decryptor)(cred)
    toAdd
  }

  def forceReplace(decryptor: decryptHandleType)(
    oldCred: Credential,
    newCred: Credential
  ): Credential = {
    newCred.addCredentialToHistory(decryptor)(oldCred)
    credentials.update(credentials.indexWhere(_.id == oldCred.id), newCred)
    newCred
  }

  def mergeAndUpdateToLatest(decryptor: decryptHandleType)(
    oldCred: Credential,
    newCred: Credential
  ): Credential = {
    val merged = newCred.getMergedIntoLatest(decryptor)(oldCred)
    credentials.update(credentials.indexWhere(_.id == oldCred.id), merged)
    merged
  }

  def addOrReplace(decryptor: decryptHandleType)(
    credNew: Credential
  ): Credential = {
    credentials.find(_.isSame(decryptor)(credNew)).foreach(
      cred => return mergeAndUpdateToLatest(decryptor)(cred, credNew)
    )
    val toAdd = {
      // Check whether we are adding it as a past-credential
      if (credNew.id >= 0)
        credNew.copy(id = getNextCredentialId)
      else {
        credNew
      }
    }
    credentials.append(toAdd)
    toAdd
  }

}
