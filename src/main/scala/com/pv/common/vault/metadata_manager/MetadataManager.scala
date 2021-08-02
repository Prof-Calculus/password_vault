/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.metadata_manager

object MetadataManager {

  def get(
    username: String,
    dbPassword: String,
    vaultFile: String,
  ): MetadataManager = {
    DbManagerImpl.getOrCreateDbManager(
      username = username,
      dbPassword = dbPassword,
      vaultFile = vaultFile
    )
  }

}

trait MetadataManager {

  def initialize(): Unit

  def getEntryByIndexOpt(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType
  ): Option[table.rowType]

  def getAllEntries(
    table: SerializedMetadataTableUtil
  ): List[table.rowType]

  def setEntryByIndex(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType,
    entry: String
  ): Unit
}
