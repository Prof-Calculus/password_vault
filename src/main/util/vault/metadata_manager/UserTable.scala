/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.vault.metadata_manager

import java.sql.ResultSet

object UserTable extends SerializedMetadataTableUtil {

  override val tableName = "USER"

  override type indexType = String

  override val _index_column_type = "VARCHAR(255)"

  override val _index_column: String = "name"

  override def transform(resultSet: ResultSet): rowType = {
    (
      resultSet.getString(1),
      resultSet.getString(2),
    )
  }

}
