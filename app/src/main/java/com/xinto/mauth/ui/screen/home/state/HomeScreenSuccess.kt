package com.xinto.mauth.ui.screen.home.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.screen.home.component.HomeAccountCard
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Composable
fun HomeScreenSuccess(
    modifier: Modifier = Modifier,
    onAccountSelect: (UUID) -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    accounts: ImmutableList<DomainAccount>,
    selectedAccounts: SnapshotStateList<UUID>,
    accountRealtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
) {
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        columns = GridCells.Adaptive(minSize = 250.dp),
    ) {
        items(
            items = accounts,
            key = { it.id }
        ) { account ->
            val realtimeData = accountRealtimeData[account.id]
            if (realtimeData != null) {
                HomeAccountCard(
                    onClick = {
                        if (selectedAccounts.isNotEmpty()) {
                            onAccountSelect(account.id)
                        }
                    },
                    onLongClick = {
                        onAccountSelect(account.id)
                    },
                    onEdit = {
                        onAccountEdit(account.id)
                    },
                    onCounterClick = {
                        onAccountCounterIncrease(account.id)
                    },
                    onCopyCode = {
                        onAccountCopyCode(account.label, realtimeData.code, it)
                    },
                    account = account,
                    realtimeData = realtimeData,
                    selected = selectedAccounts.contains(account.id)
                )
            }
        }
    }
}