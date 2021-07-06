package util.vault.memoized_metadata.user

import util.crypto.CryptoHelper.decryptHandleType
import util.crypto.CryptoHelper.encryptHandleType
import util.enums.MainMenuChoice.MainMenuChoice
import util.vault.memoized_metadata.credential.Credential
import util.vault.memoized_metadata.credential.CredentialConfigs
import util.vault.memoized_metadata.credential.CredentialManager
import util.vault.metadata_manager.MetadataManager

trait UserInterface {

  def printAndGetMainMenuInput(): MainMenuChoice

  def putCredential(
    decryptor: decryptHandleType
  )(
    credential: Credential
  ): Unit

  def getUserSpec(
    metadataManagerHandle: String => MetadataManager,
  ): UserHandler

  def getCredential(
    encrypt: encryptHandleType,
    decrypt: decryptHandleType,
  )(
    id: Int,
    descriptionOpt: Option[String] = None,
  ): Credential

  def chooseCredential(
    credentials: CredentialManager
  ): Option[Credential]

  def viewAllCredentials(
    decryptor: decryptHandleType
  )(
    credentials: CredentialConfigs
  ): Unit

}
