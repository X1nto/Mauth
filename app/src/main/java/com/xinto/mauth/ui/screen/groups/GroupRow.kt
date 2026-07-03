package com.xinto.mauth.ui.screen.groups

import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.ui.component.lazygroup.GroupedItemType
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import java.util.UUID

sealed interface GroupRow {
    val key: String

    data class GroupHeader(val group: DomainGroup) : GroupRow {
        override val key = "header_${group.id}"
    }

    data class AccountItem(val account: DomainAccount, val groupId: UUID?) : GroupRow {
        override val key = accountRowKey(account.id)
    }

    data class AddAccountRow(val groupId: UUID?) : GroupRow {
        override val key = "add_${groupId ?: "ungrouped"}"
    }

    data object UngroupedHeader : GroupRow {
        override val key = "header_ungrouped"
    }
}

fun accountRowKey(id: UUID): String = "account_$id"

data class AccountDropTarget(val groupId: UUID?)

fun buildRows(model: GroupsState): List<GroupRow> = buildList {
    for (section in model.sections) {
        when (section) {
            is GroupSection.Grouped -> add(GroupRow.GroupHeader(section.group))
            is GroupSection.Ungrouped -> add(GroupRow.UngroupedHeader)
        }
        section.accounts.forEach { add(GroupRow.AccountItem(it, section.groupId)) }
        add(GroupRow.AddAccountRow(section.groupId))
    }
}

fun buildKeyTargets(rows: List<GroupRow>): ImmutableMap<String, AccountDropTarget> =
    rows.associate { row ->
        row.key to when (row) {
            is GroupRow.GroupHeader -> AccountDropTarget(row.group.id)
            is GroupRow.AccountItem -> AccountDropTarget(row.groupId)
            is GroupRow.AddAccountRow -> AccountDropTarget(row.groupId)
            GroupRow.UngroupedHeader -> AccountDropTarget(null)
        }
    }.toImmutableMap()

fun accountType(list: List<GroupRow>, index: Int): GroupedItemType {
    val prevIsAccount = list.getOrNull(index - 1) is GroupRow.AccountItem
    val nextIsAccount = list.getOrNull(index + 1) is GroupRow.AccountItem
    return when {
        !prevIsAccount && !nextIsAccount -> GroupedItemType.Only
        !prevIsAccount -> GroupedItemType.First
        !nextIsAccount -> GroupedItemType.Last
        else -> GroupedItemType.Middle
    }
}

fun currentGroupOrder(list: List<GroupRow>) = list.filterIsInstance<GroupRow.GroupHeader>().map { it.group.id }
