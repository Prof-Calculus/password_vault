/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */

/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata.user

import com.pv.tools.crypto.CryptoHelper.decryptHandleType
import com.pv.tools.crypto.CryptoHelper.encryptHandleType
import com.pv.util.MainMenuChoice.MainMenuChoice
import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.credential.CredentialConfigs
import com.pv.common.vault.memoized_metadata.credential.CredentialManager
import com.pv.common.vault.metadata_manager.MetadataManager

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
