package com.xinto.mauth.domain.group.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class DomainGroup(
    val id: UUID,
    val name: String,
    val emoji: String?,
    val sortIndex: Int
)
