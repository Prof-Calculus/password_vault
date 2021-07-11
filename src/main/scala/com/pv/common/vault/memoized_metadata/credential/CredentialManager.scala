/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata.credential

import com.pv.tools.crypto.CryptoHelper.decryptHandleType
import com.pv.common.vault.metadata_manager.MetadataManager

object CredentialManager{

  def get(metadataManager: MetadataManager)(
    credentials: CredentialConfigs = CredentialConfigs.loadFromVault(metadataManager),
    decryptor: decryptHandleType,
  ): CredentialManager =
    new CredentialManager(
      credentials,
      decryptor,
    )

}

class CredentialManager(
  credentials: CredentialConfigs,
  decryptor: decryptHandleType,
) {
  def addOrReplace(credNew: Credential): Credential =
    credentials.addOrReplace(decryptor)(credNew)

  def merge(cred1: Credential, cred2: Credential): Credential =
    credentials.forceReplace(decryptor)(cred1, cred2)

  def getNextCredentialId: Int = credentials.getNextCredentialId

  def credentialList(): CredentialConfigs = credentials

  def remove(cred: Credential): Unit =
    credentials.remove(decryptor)(cred)

  def find(id: Int): Option[Credential] = credentials.find(_.id == id)

  def find(description: String): Option[Credential] =
    credentials.find(_.description == description)

  def isEmpty: Boolean = credentials.isEmpty
}

