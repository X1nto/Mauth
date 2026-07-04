package com.xinto.mauth.domain.group

import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.group.GroupsDao
import com.xinto.mauth.db.dao.group.entity.EntityGroup
import com.xinto.mauth.domain.group.model.DomainGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID

class GroupRepository(
    private val groupsDao: GroupsDao,
    private val accountsDao: AccountsDao
) {

    fun getGroups(): Flow<List<DomainGroup>> {
        return groupsDao.observeAll()
            .map { groups -> groups.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    suspend fun createGroup(name: String, emoji: String?): UUID {
        val sortIndex = groupsDao.nextSortIndex()
        val group = EntityGroup(
            name = name.trim(),
            emoji = emoji,
            sortIndex = sortIndex
        )
        groupsDao.upsert(group)
        return group.id
    }

    suspend fun updateGroup(id: UUID, name: String, emoji: String?) {
        val group = groupsDao.getById(id) ?: return
        groupsDao.upsert(group.copy(name = name.trim(), emoji = emoji))
    }

    suspend fun deleteGroup(id: UUID) {
        groupsDao.deleteWithMembers(id)
    }

    suspend fun reorderGroups(orderedIds: List<UUID>) {
        orderedIds.forEachIndexed { index, id ->
            groupsDao.updateSortIndex(id, index)
        }
    }

    suspend fun assignAccountsToGroup(accountIds: Set<UUID>, groupId: UUID?) {
        accountsDao.setGroup(accountIds, groupId)
    }

    private fun EntityGroup.toDomain(): DomainGroup {
        return DomainGroup(
            id = id,
            name = name,
            emoji = emoji,
            sortIndex = sortIndex
        )
    }
}
