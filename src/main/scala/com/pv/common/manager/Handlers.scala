/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.manager

import com.pv.common.manager.userinterface.UserInterface
import com.pv.common.manager.userinterface.UserInterfaceImpl
import com.pv.common.vault.CredentialManager
import com.pv.common.vault.UserHandle
import com.pv.common.vault.memoized_metadata.user.UserInfoInput
import com.pv.interaction.IoInterface
import com.pv.common.vault.metadata_manager.MetadataManager

object Handlers {

  def initialize(): Handlers = {

    // Add logic to decide whether to use GUI or Default
    val interfaceUtil: UserInterface =
      UserInterfaceImpl.get(IoInterface.getUserInterface)

    val userInfo: UserInfoInput = interfaceUtil.getUserSpec()

    val metadataManager =
      MetadataManager.get(
        userInfo.username,
        userInfo.dbPassword,
        userInfo.vaultFile
      )

    val userHandle = UserHandle.getAndPersistUserSpec(
      username = userInfo.username,
      vaultPassword = userInfo.vaultPassword,
      metadataManager = metadataManager,
      dropboxToken = UserHandle.verifyPasswordAndGetDropboxToken(
        metadataManager = metadataManager,
        username = userInfo.username,
        vaultPassword = userInfo.vaultPassword
      )
    )

    val credentialConfigs: CredentialManager =
      CredentialManager.get(metadataManager, userHandle.myCryptoHandle)()

    Handlers(
      userHandle,
      interfaceUtil,
      metadataManager,
      credentialConfigs,
    )
  }

}

case class Handlers(
  userHandle: UserHandle,
  interface: UserInterface,
  metadataManager: MetadataManager,
  credentials: CredentialManager
)
