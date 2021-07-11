/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.common.vault.memoized_metadata

import com.pv.common.vault.metadata_manager.MetadataManager

trait MemoizedMetadata extends Serializable {

  def forceSyncDownToVault(metadataManager: MetadataManager): Unit

}
