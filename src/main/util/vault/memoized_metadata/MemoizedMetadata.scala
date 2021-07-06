package util.vault.memoized_metadata

import util.vault.metadata_manager.MetadataManager

trait MemoizedMetadata extends Serializable {

  def forceSyncDownToVault(metadataManager: MetadataManager): Unit

}
