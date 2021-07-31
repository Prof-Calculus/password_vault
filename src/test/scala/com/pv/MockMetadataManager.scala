/*
 *  Created on: Aug 1, 2021
 *      Author: Srinivasan PS
 */
package com.pv


import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.SerializedMetadataTableUtil
import com.pv.common.vault.metadata_manager.UserTable
import com.pv.common.vault.metadata_manager.VaultTable
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scala.collection.mutable


object MockMetadataManager {
  def get(actualDbManager: MetadataManager): MetadataManager =
    new MockMetadataManager(actualDbManager)
}


class MockMetadataManager(
  actualDbManager: MetadataManager
) extends MetadataManager {

  var userDb: mutable.HashMap[UserTable.indexType, String] =
    new mutable.HashMap[UserTable.indexType, String]()

  var vaultDb: mutable.HashMap[VaultTable.indexType, String] =
    new mutable.HashMap[VaultTable.indexType, String]()

  def initialize(): Unit = actualDbManager.initialize()

  def getEntryByIndexOpt(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType
  ): Option[table.rowType] = {
    val actual = actualDbManager.getEntryByIndexOpt(table)(id)

    val mock = if(table.tableName == UserTable.tableName) {
      userDb.get(id.asInstanceOf[UserTable.indexType]).map(
        entry => (id, entry))
    } else if(table.tableName == VaultTable.tableName) {
      vaultDb.get(id.asInstanceOf[VaultTable.indexType]).map(
        entry => (id, entry))
    } else {
      None
    }

    actual shouldBe mock

    mock
  }

  def getAllEntries(
    table: SerializedMetadataTableUtil
  ): List[table.rowType] = {
    val actual = actualDbManager.getAllEntries(table)

    val mock = if(table.tableName == UserTable.tableName) {
      userDb.map{
        case (id, entry) => (id.asInstanceOf[table.indexType], entry)
      }.toList
    } else if(table.tableName == VaultTable.tableName) {
      vaultDb.map{
        case (id, entry) => (id.asInstanceOf[table.indexType], entry)
      }.toList
    } else {
      List.empty
    }

    actual shouldBe mock

    mock
  }

  def setEntryByIndex(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType,
    entry: String
  ): Unit = {
    if(table.tableName == UserTable.tableName) {
      userDb.update(id.asInstanceOf[UserTable.indexType], entry)
    } else if(table.tableName == VaultTable.tableName) {
      vaultDb.update(id.asInstanceOf[VaultTable.indexType], entry)
    }
  }

}