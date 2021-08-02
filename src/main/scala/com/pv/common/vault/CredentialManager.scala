/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault

import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.credential.CredentialConfigs
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.tools.crypto.MyCryptoHandle

object CredentialManager{

  def get(metadataManager: MetadataManager, myCrypto: MyCryptoHandle)(
    credentials: CredentialConfigs =
      CredentialConfigs.loadFromVault(metadataManager, myCrypto),
  ): CredentialManager = new CredentialManager(myCrypto)(credentials)

}

class CredentialManager(myCrypto: MyCryptoHandle)(
  credentials: CredentialConfigs
) {
  def addOrReplace(credNew: Credential): Credential =
    credentials.addOrReplace(myCrypto)(credNew)

  def merge(cred1: Credential, cred2: Credential): Credential =
    credentials.forceReplace(myCrypto)(cred1, cred2)

  def getNextCredentialId: Int = credentials.getNextCredentialId

  def credentialList(): CredentialConfigs = credentials

  def remove(cred: Credential): Unit =
    credentials.remove(myCrypto)(cred)

  def find(id: Int): Option[Credential] = credentials.find(_.id == id)

  def find(description: String): Option[Credential] =
    credentials.find(_.description == description)

  def isEmpty: Boolean = credentials.isEmpty

  def getLastUpdateTimestamp: Long = credentials.timestamp
}

