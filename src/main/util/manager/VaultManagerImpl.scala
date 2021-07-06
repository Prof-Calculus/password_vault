package util.manager

import util.vault.memoized_metadata.credential.Credential

object VaultManagerImpl {

  def initialize(handlers: Handlers): VaultManagerImpl =
    new VaultManagerImpl(handlers)

}

class VaultManagerImpl(
  handlers: Handlers,
) extends VaultManager {

  private def getCredentialChoiceOpt: Option[Credential] =
    handlers
      .interface
      .chooseCredential(handlers.credentials)

  override def addCredential(): Unit =
    handlers
      .credentials
      .addOrReplace(
        handlers.interface.getCredential(
          handlers.userHandle.encryptHandle,
          handlers.userHandle.decryptHandle,
        )(
          id = handlers.credentials.getNextCredentialId
        )
      )
      .forceSyncDownToVault(handlers.metadataManager)

  override def viewCredential(): Unit =
    getCredentialChoiceOpt
      .foreach(handlers.interface.putCredential(
        handlers.userHandle.decryptHandle))

  override def listAllCredentials(): Unit =
    handlers
      .interface
      .viewAllCredentials(handlers.userHandle.decryptHandle)(
        handlers.credentials.credentialList())

  override def editCredential(): Unit =
    getCredentialChoiceOpt
      .foreach(
        cred =>
          handlers.credentials.merge(
            cred,
            handlers
              .interface
              .getCredential(
                handlers.userHandle.encryptHandle,
                handlers.userHandle.decryptHandle,
              )(id = cred.id)
          ).forceSyncDownToVault(handlers.metadataManager)
      )

  override def deleteCredential(): Unit = ???

  override def syncWithDropbox(): Unit = ???

}
