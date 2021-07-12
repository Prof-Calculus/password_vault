/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault

import com.pv.common.vault.memoized_metadata.user.UserInfo
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.UserTable
import com.pv.tools.crypto.CryptoHelper
import com.pv.tools.crypto.CryptoHelper.EncryptedString
import com.pv.tools.crypto.CryptoHelper.TransformedString
import com.pv.tools.crypto.MyCryptoHandle


object UserHandle {

  def verifyPasswordAndGetDropboxToken(
    username: String,
    metadataManager: MetadataManager,
    vaultPassword: TransformedString,
  ): EncryptedString =
    CryptoHelper.encrypt(
      metadataManager
        .getEntryByIndexOpt(UserTable)(username)
        .map(_._2)
        .map(CryptoHelper.decrypt(_, vaultPassword))
        .getOrElse(""),
      vaultPassword
    )

  def getAndPersistUserSpec(
    metadataManager: MetadataManager,
    username: String,
    vaultPassword: TransformedString,
    dropboxToken: EncryptedString
  ): UserHandle = {
    val handle = new UserHandle(vaultPassword)(
      metadataManager,
      UserInfo.get(username = username, dropboxToken = dropboxToken),
    )
    handle.forceSyncDownToVault(metadataManager)
    handle
  }

}

class UserHandle(private val vaultPassword: TransformedString)(
  metadataManager: MetadataManager,
  userInfo: UserInfo,
  val myCryptoHandle: MyCryptoHandle = MyCryptoHandle(vaultPassword)
) {

  def forceSyncDownToVault(metadataManager: MetadataManager): Unit = {
    userInfo.forceSyncDownToVault(metadataManager, myCryptoHandle)
  }

  def getMetadataManager: MetadataManager = metadataManager

  def getUserInfo: UserInfo = userInfo
}
