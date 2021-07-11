/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata.user

import org.json4s.DefaultFormats
import org.json4s.Formats
import org.json4s.native.Serialization.read
import org.json4s.native.Serialization.writePretty
import com.pv.tools.crypto.CryptoHelper.EncryptedString
import com.pv.common.vault.memoized_metadata.MemoizedMetadata
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.UserTable


object UserInfo extends Serializable {
  implicit val formats: Formats = DefaultFormats

  def get(
    username: String,
    dropboxTokenOpt: Option[EncryptedString] = None
  ): UserInfo =
    UserInfo(
      username = username,
      dropboxTokenOpt = dropboxTokenOpt
    )

  def fromString(str: String): UserInfo = read[UserInfo](str)
}

case class UserInfo(
  username: String,
  dropboxTokenOpt: Option[EncryptedString] = None,
) extends MemoizedMetadata {

  implicit val formats: Formats = UserInfo.formats

  override def toString: String = writePretty[UserInfo](this)

  override def forceSyncDownToVault(metadataManager: MetadataManager): Unit =
    metadataManager.setEntryByIndex(UserTable)(
      username,
      dropboxTokenOpt.getOrElse("")
    )

}