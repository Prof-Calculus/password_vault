package util.manager

import util.action.IoInterface
import util.vault.memoized_metadata.credential.CredentialManager
import util.vault.memoized_metadata.user.UserHandler
import util.vault.memoized_metadata.user.UserInterface
import util.vault.memoized_metadata.user.UserInterfaceImpl
import util.vault.metadata_manager.MetadataManager

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
