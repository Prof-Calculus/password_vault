/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.metadata_manager

object MetadataManager {

  def getOptimalMetadataManager(
    username: String,
  ): MetadataManager = {
    DbManagerImpl.getOrCreateDbManager(username)
  }

}

trait MetadataManager {

  val vaultFile: String

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
