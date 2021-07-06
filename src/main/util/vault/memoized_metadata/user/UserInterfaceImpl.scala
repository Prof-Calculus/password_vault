package util.vault.memoized_metadata.user

import util.action.IoInterface
import util.crypto.CryptoHelper
import util.crypto.CryptoHelper.decryptHandleType
import util.crypto.CryptoHelper.encryptHandleType
import util.enums.MainMenuChoice
import util.enums.MainMenuChoice.MainMenuChoice
import util.vault.memoized_metadata.credential.Credential
import util.vault.memoized_metadata.credential.CredentialConfigs
import util.vault.memoized_metadata.credential.CredentialManager
import util.vault.metadata_manager.MetadataManager

object UserInterfaceImpl {

  def get(interface: IoInterface): UserInterfaceImpl =
    new UserInterfaceImpl(interface)

}

class UserInterfaceImpl(interface: IoInterface) extends UserInterface {

  def printAndGetMainMenuInput(): MainMenuChoice = {
    MainMenuChoice.values.foreach(
      choice => print(s"Enter ${choice.id} to $choice\n")
    )
    val inp = interface.getString("Your input").toInt
    MainMenuChoice.values.find(
      _.id == inp
    ).getOrElse(
      throw new RuntimeException("Illegal input")
    )
  }

  def putCredential(
    decryptor: decryptHandleType
  )(
    credential: Credential
  ): Unit = {
    interface.putString(s"==== Credential start==== ${credential.id}")
    interface.putString(credential.getDescription, "Description")
    interface.putPasswordString(credential.getUserEntry(decryptor), "Username")
    interface.putPasswordString(credential.getPasswordEntry(decryptor), "Password")
    interface.putString(s"Old Credentials for ${credential.id}")
    credential.pastCredentials.foreach(putCredential(decryptor))
    interface.putString(s"==== Credential end ======\n")
  }

  def getUserSpec(
    metadataManagerHandle: String => MetadataManager,
  ): UserHandler = {
    val username: String = interface.getString("Username")
    val vaultPassword: String = interface.getPasswordInput("Password")

    CryptoHelper.definitiveTransformation(vaultPassword)

    UserHandler.getAndPersistUserSpec(
      username = username,
      vaultPassword = vaultPassword,
      metadataManager = metadataManagerHandle(username)
    )
  }

  def getCredential(
    encrypt: encryptHandleType,
    decrypt: decryptHandleType
  )(
    id: Int,
    descriptionOpt: Option[String] = None,
  ): Credential = {
    interface.putString("Add the credential")
    Credential.getCredential(
      id,
      description =
        descriptionOpt.getOrElse(
          interface.getString("Enter description")
        ),
      userEntry =
        encrypt(
          interface.getPasswordInput("Enter credential username")
        ),
      passwordEntry =
        encrypt(
          interface.getPasswordInput("Enter credential password")
        )
    )

  }

  def chooseCredential(
    credentials: CredentialManager
  ): Option[Credential] = {
    interface.putString("Choose a credential")

    if(credentials.isEmpty) {
      interface.putString("Empty")
      return None
    }

    credentials.credentialList().foreach(
      cred =>
        interface.putString(s"Choose ${cred.id} for ${cred.getDescription}")
    )
    credentials.find(interface.getString("Your input").toInt)

  }

  def viewAllCredentials(
    decryptor: decryptHandleType
  )(
    credentials: CredentialConfigs
  ): Unit = {
    interface.putString("Viewing all credentials")

    if(credentials.isEmpty) {
      interface.putString("Empty")
      return
    }
    credentials.foreach(putCredential(decryptor))

  }

}
