package com.backlogorder.domain.sort

import com.backlogorder.domain.model.ListEntry

data class FranchiseGroup(
    val id: String,
    val key: String?,
    val displayName: String = key ?: "Standalone",
    val entries: List<ListEntry>,
) {
    val isFranchise: Boolean get() = key != null && !key.startsWith("standalone:")
}
