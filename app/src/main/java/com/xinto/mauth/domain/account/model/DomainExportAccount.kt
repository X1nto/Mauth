package com.xinto.mauth.domain.account.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
sealed class DomainExportAccount {
    abstract val id: UUID
    abstract val icon: Uri?
    abstract val label: String
    abstract val issuer: String

    val shortLabel by lazy {
        issuer.ifEmpty { label }.take(1)
    }

    @Immutable
    data class Totp(
        override val id: UUID,
        override val icon: Uri?,
        override val label: String,
        override val issuer: String,
        val period: Int
    ) : DomainExportAccount()

    @Immutable
    data class Hotp(
        override val id: UUID,
        override val icon: Uri?,
        override val label: String,
        override val issuer: String,
        val counter: Int
    ) : DomainExportAccount()

}
