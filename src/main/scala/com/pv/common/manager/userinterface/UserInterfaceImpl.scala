/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.manager.userinterface

import com.pv.common.vault.CredentialManager
import com.pv.common.vault.UserHandle
import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.credential.CredentialConfigs
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.interaction.IoInterface
import com.pv.tools.crypto.CryptoHelper
import com.pv.tools.crypto.CryptoHelper.TransformedString
import com.pv.tools.crypto.MyCryptoHandle
import com.pv.util.MainMenuChoice
import com.pv.util.MainMenuChoice.MainMenuChoice

object UserInterfaceImpl {

  def get(interface: IoInterface): UserInterfaceImpl =
    new UserInterfaceImpl(interface)

}

class UserInterfaceImpl(interface: IoInterface) extends UserInterface {

  def printAndGetMainMenuInput(): MainMenuChoice = {
    MainMenuChoice.values.foreach(
      choice => print(s"Enter ${choice.id} to $choice\n")
    )
    val input = interface.getString("Your input").toInt
    MainMenuChoice.values.find(_.id == input).getOrElse(
      throw new RuntimeException("Illegal input")
    )
  }

  def putCredential(myCrypto: MyCryptoHandle)(
    credential: Credential
  ): Unit = {
    interface.putString(s"==== Credential start==== ${credential.id}")
    interface.putString(credential.getDescription, "Description")
    interface.putPasswordString(credential.getUserEntry(myCrypto), "Username")
    interface.putPasswordString(credential.getPasswordEntry(myCrypto), "Password")
    interface.putString(s"Old Credentials for ${credential.id}")
    credential.pastCredentials.foreach(putCredential(myCrypto))
    interface.putString(s"==== Credential end ======\n")
  }

  def getUserSpec(
    metadataManagerHandle: String => MetadataManager,
  ): UserHandle = {
    val username: String = interface.getString("Username")
    val vaultPassword: TransformedString =
      CryptoHelper.definitiveTransformation(
        interface.getPasswordInput("Vault Password"))
    val mm = metadataManagerHandle(username)

    UserHandle.getAndPersistUserSpec(
      username = username,
      vaultPassword = vaultPassword,
      metadataManager = mm,
      dropboxToken = UserHandle.verifyPasswordAndGetDropboxToken(
        metadataManager = mm,
        username = username,
        vaultPassword = vaultPassword
      )
    )
  }

  def getCredential(myCrypto: MyCryptoHandle)(
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
        myCrypto.encrypt(
          interface.getPasswordInput("Enter credential username")
        ),
      passwordEntry =
        myCrypto.encrypt(
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

  def viewAllCredentials(myCrypto: MyCryptoHandle)(
    credentials: CredentialConfigs
  ): Unit = {
    interface.putString("Viewing all credentials")

    if(credentials.isEmpty) {
      interface.putString("Empty")
      return
    }
    credentials.foreach(putCredential(myCrypto))
  }

}
