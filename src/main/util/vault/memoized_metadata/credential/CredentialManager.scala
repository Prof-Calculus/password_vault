package util.vault.memoized_metadata.credential

import util.crypto.CryptoHelper.decryptHandleType
import util.vault.metadata_manager.MetadataManager

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

