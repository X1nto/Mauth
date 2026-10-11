package com.xinto.mauth.domain.backup.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.backup.ImportFailure
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.account.model.monogramOf
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Immutable
@Parcelize
data class DomainImportCandidate(
    val account: DomainAccountInfo,
    val groupName: String?,
    val groupEmoji: String?,
    val isDuplicate: Boolean
) : Parcelable {

    val id: UUID
        get() = account.id

}

@Immutable
@Parcelize
data class DomainRejectedEntry(
    val name: String,
    val issuer: String,
    val reason: ImportFailure
) : Parcelable {

    val shortLabel: String
        get() = monogramOf(issuer, name)

}
