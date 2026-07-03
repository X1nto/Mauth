package com.xinto.mauth.ui.screen.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.group.GroupRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GroupsViewModel(
    private val groups: GroupRepository,
    private val accounts: AccountRepository
) : ViewModel() {

    val uiModel = combine(groups.getGroups(), accounts.getGroupedAccounts()) { groupList, buckets ->
        GroupsState(
            sections = buildList {
                groupList.forEach { group ->
                    add(
                        GroupSection.Grouped(
                            group = group,
                            accounts = (buckets[group.id] ?: emptyList()).toImmutableList()
                        )
                    )
                }
                add(GroupSection.Ungrouped((buckets[null] ?: emptyList()).toImmutableList()))
            }.toImmutableList()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupsState(persistentListOf())
    )

    fun createGroup(name: String, emoji: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            groups.createGroup(name, emoji)
        }
    }

    fun updateGroup(id: UUID, name: String, emoji: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            groups.updateGroup(id, name, emoji)
        }
    }

    fun deleteGroup(id: UUID) {
        viewModelScope.launch {
            groups.deleteGroup(id)
        }
    }

    fun moveAccountToGroup(accountId: UUID, groupId: UUID?) {
        viewModelScope.launch {
            groups.assignAccountsToGroup(setOf(accountId), groupId)
        }
    }

    fun reorderGroups(orderedIds: List<UUID>) {
        viewModelScope.launch {
            groups.reorderGroups(orderedIds)
        }
    }

    fun moveUp(id: UUID) = move(id, -1)

    fun moveDown(id: UUID) = move(id, 1)

    private fun move(id: UUID, delta: Int) {
        val current = uiModel.value.groupSections.map { it.group }
        val index = current.indexOfFirst { it.id == id }
        if (index < 0) return
        val target = index + delta
        if (target < 0 || target > current.lastIndex) return
        val reordered = current.toMutableList().apply {
            add(target, removeAt(index))
        }
        viewModelScope.launch {
            groups.reorderGroups(reordered.map { it.id })
        }
    }
}
