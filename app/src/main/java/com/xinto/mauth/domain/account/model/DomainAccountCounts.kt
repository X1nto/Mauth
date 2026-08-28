package com.xinto.mauth.domain.account.model

import androidx.compose.runtime.Immutable
import com.xinto.mauth.domain.group.model.GroupFilter
import kotlinx.collections.immutable.persistentMapOf
import java.util.UUID

@Immutable
data class DomainAccountCounts(
    val total: Int,
    val ungrouped: Int,
    val byGroup: Map<UUID, Int>
) {

    operator fun get(filter: GroupFilter): Int {
        return when (filter) {
            GroupFilter.All -> total
            GroupFilter.Ungrouped -> ungrouped
            is GroupFilter.Specific -> byGroup[filter.id] ?: 0
        }
    }

    companion object {
        val Empty = DomainAccountCounts(total = 0, ungrouped = 0, byGroup = persistentMapOf())
    }
}
