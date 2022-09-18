package com.xinto.mauth.domain.model

import java.util.*

data class DomainAccount(
    val id: UUID,
    val label: String,
    val secret: String
) {
    val idString = id.toString()
}
