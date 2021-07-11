/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.metadata_manager

import java.sql.ResultSet

trait SerializedMetadataTableUtil {

  val tableName: String

  type indexType

  type rowType = (indexType, String)

  val _index_column: String

  val _index_column_type: String

  val _entry_column: String = "entry"

  def transform(resultSet: ResultSet): rowType

  def selectAllSql: String = s"SELECT * from ${this.tableName}"

  def insertSql(name: indexType, entry: String): String =
    s"INSERT or REPLACE INTO ${tableName} " +
      s"(${_index_column}, ${_entry_column}) " +
      s"VALUES " +
      s"('$name', '$entry')"

  def selectByIndexSql(index: indexType): String =
    s"SELECT * from ${tableName} where ${_index_column}='${index}'"

  def createTableSql: String =
    s"CREATE TABLE ${tableName} " +
      s"(${_index_column} ${_index_column_type} not NULL, " +
      s"${_entry_column} VARCHAR(255) not NULL, " +
      s"PRIMARY KEY ( ${_index_column} ))"

}
