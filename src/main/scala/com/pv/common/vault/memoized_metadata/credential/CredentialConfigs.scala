/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata.credential

import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.VaultTable
import com.pv.tools.crypto.MyCryptoHandle
import java.util.Date
import org.json4s.CustomSerializer
import org.json4s.DefaultFormats
import org.json4s.Formats
import org.json4s.JString
import org.json4s.native.Serialization.read
import org.json4s.native.Serialization.writePretty
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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

  def loadFromVault(
    metadataManager: MetadataManager,
    myCrypto: MyCryptoHandle,
  ): CredentialConfigs = {
    CredentialConfigs(
      metadataManager.getAllEntries(VaultTable).map {
        case (id, entry) => Credential.loadFromVaultEntry(myCrypto)(id, entry)
      }.to[ListBuffer]
    )
  }

  def initializeEmpty(): CredentialConfigs =
    CredentialConfigs(ListBuffer.empty[Credential])

}


case class CredentialConfigs(
  credentials: ListBuffer[Credential] = ListBuffer.empty[Credential],
  var timestamp: Long = new Date().getTime
) extends mutable.Iterable[Credential] with Serializable {

  private def updateTimestamp(): Unit = timestamp = new Date().getTime

  implicit val formats: Formats = CredentialConfigs.formats

  override def toString: String = writePretty(this.credentials)

  def clear(): Unit = credentials.clear()

  override def iterator: Iterator[Credential] = credentials.iterator

  def merge(myCrypto: MyCryptoHandle)(
    newCredentials: CredentialConfigs
  ): Unit = {
    newCredentials.foreach(addOrReplace(myCrypto))
    updateTimestamp()
  }

  def length: Int = credentials.length

  override def isEmpty: Boolean = credentials.isEmpty

  def getNextCredentialId: Int = length

  def remove(myCrypto: MyCryptoHandle)(
    cred: Credential,
  ): Credential = {
    val toAdd = cred.getMergedIntoLatest(myCrypto)(cred)
    updateTimestamp()
    toAdd
  }

  def forceReplace(myCrypto: MyCryptoHandle)(
    oldCred: Credential,
    newCred: Credential
  ): Credential = {
    newCred.addCredentialToHistory(myCrypto)(oldCred)
    credentials.update(credentials.indexWhere(_.id == oldCred.id), newCred)
    updateTimestamp()
    newCred
  }

  def mergeAndUpdateToLatest(myCrypto: MyCryptoHandle)(
    oldCred: Credential,
    newCred: Credential
  ): Credential = {
    val merged = newCred.getMergedIntoLatest(myCrypto)(oldCred)
    credentials.update(credentials.indexWhere(_.id == oldCred.id), merged)
    merged
  }

  def addOrReplace(myCrypto: MyCryptoHandle)(
    credNew: Credential
  ): Credential = {
    credentials.find(_.isSame(myCrypto)(credNew)).foreach(
      cred => return mergeAndUpdateToLatest(myCrypto)(cred, credNew)
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
    updateTimestamp()
    toAdd
  }

}
