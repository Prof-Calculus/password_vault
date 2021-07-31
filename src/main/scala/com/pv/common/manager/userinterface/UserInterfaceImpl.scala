/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.manager.userinterface

import com.pv.common.vault.CredentialManager
import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.credential.CredentialConfigs
import com.pv.common.vault.memoized_metadata.user.UserInfoInput
import com.pv.interaction.IoInterface
import com.pv.tools.crypto.CryptoHelper
import com.pv.tools.crypto.CryptoHelper.TransformedString
import com.pv.tools.crypto.MyCryptoHandle
import com.pv.util.Constants
import com.pv.util.MainMenuChoice
import com.pv.util.MainMenuChoice.MainMenuChoice

object UserInterfaceImpl {

  def get(interface: IoInterface): UserInterfaceImpl =
    new UserInterfaceImpl(interface)

}

class UserInterfaceImpl(interface: IoInterface) extends UserInterface {

  override def printAndGetMainMenuInput(): MainMenuChoice = {
    MainMenuChoice.values.foreach(
      choice => print(s"Enter ${choice.id} to $choice\n")
    )
    val input = interface.getString("Your input").toInt
    MainMenuChoice.values.find(_.id == input).getOrElse(
      throw new RuntimeException("Illegal input")
    )
  }

  override def putCredential(myCrypto: MyCryptoHandle)(
    credential: Credential,
    indent: String = ""
  ): Unit = {
    interface.putString(s"$indent==== Credential start ==== ${credential.id}")
    interface.putString(
      indent + credential.getDescription,
      "Description")
    interface.putPasswordString(
      indent + credential.getUserEntry(myCrypto),
      "Username")
    interface.putPasswordString(
      indent + credential.getPasswordEntry(myCrypto),
      "Password")
    interface.putString(s"Old Credentials for ${credential.id}")
    credential.pastCredentials.foreach(
      c => putCredential(myCrypto)(c, indent + "\t"))
    interface.putString(s"$indent==== Credential end ======\n")
  }

  override def getUserSpec(): UserInfoInput = {
    val username: String = interface.getString("Username")

    val vaultFileTemp: String = interface.getString(
      s"Enter vault file (default)[${Constants.vaultFile(username)}]")

    val vaultFile =
      if (vaultFileTemp.isEmpty)
        s"${Constants.vaultFile(username)}"
      else
        vaultFileTemp

    val dbPassword: TransformedString =
      interface.getPasswordInput("A Database password")

    val vaultPassword: TransformedString =
      CryptoHelper.definitiveTransformation(
        interface.getPasswordInput("The Vault Password"))

    UserInfoInput(
      username = username,
      vaultFile = vaultFile,
      dbPassword = dbPassword,
      vaultPassword = vaultPassword
    )
  }

  override def getCredential(myCrypto: MyCryptoHandle)(
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

  override def chooseCredential(
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

  override def viewAllCredentials(myCrypto: MyCryptoHandle)(
    credentials: CredentialConfigs
  ): Unit = {
    interface.putString("Viewing all credentials")

    if(credentials.isEmpty) {
      interface.putString("Empty")
      return
    }
    credentials.foreach(c => putCredential(myCrypto)(c))
  }

}
