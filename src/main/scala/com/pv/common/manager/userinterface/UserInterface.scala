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
import com.pv.tools.crypto.MyCryptoHandle
import com.pv.util.MainMenuChoice.MainMenuChoice

trait UserInterface {

  def printAndGetMainMenuInput(): MainMenuChoice

  def putCredential(myCrypto: MyCryptoHandle)(credential: Credential): Unit

  def getUserSpec(
    metadataManagerHandle: String => MetadataManager,
  ): UserHandle

  def getCredential(myCrypto: MyCryptoHandle)(
    id: Int,
    descriptionOpt: Option[String] = None,
  ): Credential

  def chooseCredential(
    credentials: CredentialManager
  ): Option[Credential]

  def viewAllCredentials(myCrypto: MyCryptoHandle)(
    credentials: CredentialConfigs
  ): Unit

}
