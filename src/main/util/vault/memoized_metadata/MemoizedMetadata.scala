/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.vault.memoized_metadata

import util.vault.metadata_manager.MetadataManager

trait MemoizedMetadata extends Serializable {

  def forceSyncDownToVault(metadataManager: MetadataManager): Unit

}
