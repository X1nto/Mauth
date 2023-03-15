package com.xinto.mauth.ui.screen.home.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.screen.home.component.HomeAccountCard
import java.util.UUID

@Composable
fun HomeScreenSuccess(
    onAccountSelect: (UUID) -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String) -> Unit,
    accounts: List<DomainAccount>,
    selectedAccounts: List<UUID>,
    accountRealtimeData: Map<UUID, DomainOtpRealtimeData>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(accounts) { account ->
            val realtimeData = accountRealtimeData[account.id]
            if (realtimeData != null) {
                HomeAccountCard(
                    onSelect = {
                        onAccountSelect(account.id)
                    },
                    onEdit = {
                        onAccountEdit(account.id)
                    },
                    onCounterClick = {
                        onAccountCounterIncrease(account.id)
                    },
                    onCopyCode = {
                        onAccountCopyCode(account.label, realtimeData.code)
                    },
                    account = account,
                    realtimeData = realtimeData,
                    selected = selectedAccounts.contains(account.id)
                )
            }
        }
    }
}