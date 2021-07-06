/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.vault.metadata_manager

import java.sql.ResultSet

object VaultTable extends SerializedMetadataTableUtil {

  override val tableName = "VAULT"

  override type indexType = Int

  override val _index_column: String = "id"

  override val _index_column_type = "INTEGER"

  override def transform(resultSet: ResultSet): rowType = {
    (
      resultSet.getInt(1),
      resultSet.getString(2),
    )
  }

}
