/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.vault.memoized_metadata.user

import util.crypto.CryptoHelper
import util.crypto.CryptoHelper.EncryptedString
import util.crypto.CryptoHelper.TransformedString
import util.crypto.CryptoHelper.decryptHandleType
import util.crypto.CryptoHelper.encryptHandleType
import util.vault.metadata_manager.MetadataManager
import util.vault.metadata_manager.UserTable


object UserHandler {
  private def getDropboxTokenFromVault(
    username: String,
    metadataManager: MetadataManager,
  ): Option[EncryptedString] =
      metadataManager
        .getEntryByIndexOpt(UserTable)(username)
        .map(_._2)
        .find(_.nonEmpty)  // No token is written to DB as ''

  def getAndPersistUserSpec(
    metadataManager: MetadataManager,
    username: String,
    vaultPassword: TransformedString,
    dropboxTokenOpt: Option[EncryptedString] = None
  ): UserHandler = {
    val handle = new UserHandler(
      metadataManager,
      UserInfo.get(
        username = username,
        dropboxTokenOpt =
          dropboxTokenOpt match {
            case None =>
              getDropboxTokenFromVault(
                username,
                metadataManager
              )
            case Some(v) => Some(v)
          }
      ),
      vaultPassword,
    )
    handle.forceSyncDownToVault(metadataManager)
    handle
  }

}

class UserHandler(
  metadataManager: MetadataManager,
  userInfo: UserInfo,
  private val vaultPassword: TransformedString
) {

  def forceSyncDownToVault(metadataManager: MetadataManager): Unit = {
    userInfo.forceSyncDownToVault(metadataManager)
  }

  val decryptHandle: decryptHandleType = {
    str: EncryptedString => CryptoHelper.decrypt(str, vaultPassword)
  }

  val encryptHandle: encryptHandleType = {
    str: EncryptedString => CryptoHelper.encrypt(str, vaultPassword)
  }

  def getMetadataManager: MetadataManager = metadataManager

  def getUserInfo: UserInfo = userInfo
}
