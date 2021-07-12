/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.manager

import com.pv.common.manager.userinterface.UserInterface
import com.pv.common.manager.userinterface.UserInterfaceImpl
import com.pv.common.vault.CredentialManager
import com.pv.common.vault.UserHandle
import com.pv.interaction.IoInterface
import com.pv.common.vault.metadata_manager.MetadataManager

object Handlers {

  def initialize(): Handlers = {

    // Add logic to decide whether to use GUI or Default
    val interfaceUtil: UserInterface =
      UserInterfaceImpl.get(IoInterface.getUserInterface)
    val userHandle: UserHandle =
      interfaceUtil.getUserSpec(MetadataManager.getOptimalMetadataManager)
    val metadataManager: MetadataManager =
      userHandle.getMetadataManager
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
