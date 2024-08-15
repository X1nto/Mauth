package com.xinto.mauth.domain.account.model

import android.net.Uri
import java.util.UUID

data class DomainExportAccount(
    val id: UUID,
    val icon: Uri?,
    val label: String,
    val issuer: String,
    val url: String
) {
    val shortLabel = label.take(1)
}