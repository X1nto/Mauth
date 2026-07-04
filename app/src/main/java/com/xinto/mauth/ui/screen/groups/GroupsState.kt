package com.xinto.mauth.ui.screen.groups

import androidx.compose.runtime.Immutable
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.group.model.DomainGroup
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Immutable
sealed interface GroupSection {
    val accounts: ImmutableList<DomainAccount>
    val groupId: UUID?

    data class Grouped(
        val group: DomainGroup,
        override val accounts: ImmutableList<DomainAccount>
    ) : GroupSection {
        override val groupId: UUID get() = group.id
    }

    data class Ungrouped(override val accounts: ImmutableList<DomainAccount>) : GroupSection {
        override val groupId: UUID? get() = null
    }
}

@Immutable
data class GroupsState(val sections: ImmutableList<GroupSection>)

val GroupsState.groupSections: List<GroupSection.Grouped>
    get() = sections.filterIsInstance<GroupSection.Grouped>()

val GroupsState.ungroupedCount: Int
    get() = sections.filterIsInstance<GroupSection.Ungrouped>().sumOf { it.accounts.size }

val GroupsState.isEmpty: Boolean
    get() = sections.none { it is GroupSection.Grouped } && sections.all { it.accounts.isEmpty() }
