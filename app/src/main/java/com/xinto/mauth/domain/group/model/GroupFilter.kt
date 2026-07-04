package com.xinto.mauth.domain.group.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
sealed interface GroupFilter {

    fun matches(groupId: UUID?): Boolean

    @Immutable
    data object All : GroupFilter {
        override fun matches(groupId: UUID?) = true
    }

    @Immutable
    data object Ungrouped : GroupFilter {
        override fun matches(groupId: UUID?) = groupId == null
    }

    @Immutable
    data class Specific(val id: UUID) : GroupFilter {
        override fun matches(groupId: UUID?) = groupId == id
    }
}
