/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.manager

import com.pv.interaction.IoInterface
import com.pv.common.vault.memoized_metadata.credential.CredentialManager
import com.pv.common.vault.memoized_metadata.user.UserHandler
import com.pv.common.vault.memoized_metadata.user.UserInterface
import com.pv.common.vault.memoized_metadata.user.UserInterfaceImpl
import com.pv.common.vault.metadata_manager.MetadataManager

object Handlers {

  def initialize(): Handlers = {

    // Add logic to decide whether to use GUI or Default
    val interfaceUtil: UserInterface =
      UserInterfaceImpl.get(IoInterface.getUserInterface)
    val userHandle: UserHandler =
      interfaceUtil.getUserSpec(MetadataManager.getOptimalMetadataManager)
    val metadataManager: MetadataManager =
      userHandle.getMetadataManager
    val credentialConfigs: CredentialManager =
      CredentialManager.get(metadataManager)(
        decryptor = userHandle.decryptHandle)

    Handlers(
      userHandle,
      interfaceUtil,
      metadataManager,
      credentialConfigs,
    )
  }

}

case class Handlers(
  userHandle: UserHandler,
  interface: UserInterface,
  metadataManager: MetadataManager,
  credentials: CredentialManager
)
