/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.manager

import com.pv.common.vault.memoized_metadata.credential.Credential

object VaultManagerImpl {

  def initialize(handlers: Handlers): VaultManagerImpl =
    new VaultManagerImpl(handlers)

}

class VaultManagerImpl(
  handlers: Handlers,
) extends VaultManager {

  private def getCredentialChoiceOpt: Option[Credential] =
    handlers.interface.chooseCredential(handlers.credentials)

  override def addCredential(): Unit =
    handlers
      .credentials
      .addOrReplace(
        handlers.interface.getCredential(
          handlers.userHandle.myCryptoHandle,
        )(id = handlers.credentials.getNextCredentialId)
      )
      .forceSyncDownToVault(
        handlers.metadataManager,
        handlers.userHandle.myCryptoHandle
      )

  override def viewCredential(): Unit =
    getCredentialChoiceOpt
      .foreach(c =>
        handlers.interface.putCredential(handlers.userHandle.myCryptoHandle)(c))

  override def listAllCredentials(): Unit =
    handlers
      .interface
      .viewAllCredentials(handlers.userHandle.myCryptoHandle)(
        handlers.credentials.credentialList()
      )

  override def editCredential(): Unit =
    getCredentialChoiceOpt
      .foreach(
        cred =>
          handlers.credentials.merge(
            cred,
            handlers
              .interface
              .getCredential(handlers.userHandle.myCryptoHandle)(id = cred.id)
          ).forceSyncDownToVault(
            handlers.metadataManager,
            handlers.userHandle.myCryptoHandle
          )
      )

  override def deleteCredential(): Unit = ???

  override def syncWithDropbox(): Unit = ???

}
