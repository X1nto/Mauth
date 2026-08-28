package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.domain.settings.model.AccountsLayout
import com.xinto.mauth.domain.account.model.DomainAccountCounts
import com.xinto.mauth.domain.settings.model.AccountsSort
import com.xinto.mauth.domain.group.model.GroupFilter
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.screen.home.HomeScreen
import com.xinto.mauth.ui.screen.home.HomeScreenState
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

class HomeScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun Empty() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Empty,
            accountRealtimeData = remember { mutableStateMapOf<UUID, DomainOtpRealtimeData>() },
            selectedAccounts = remember { mutableStateListOf<UUID>() },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = DomainAccountCounts.Empty,
            accountsLayout = AccountsLayout.Cards,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun CardsWithAccounts() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Success(PreviewFixtures.accounts),
            accountRealtimeData = PreviewFixtures.rememberRealtimeData(),
            selectedAccounts = remember { mutableStateListOf<UUID>() },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = PreviewFixtures.accountCounts,
            accountsLayout = AccountsLayout.Cards,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun CardsAccountSelection() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Success(PreviewFixtures.accounts),
            accountRealtimeData = PreviewFixtures.rememberRealtimeData(),
            selectedAccounts = remember { mutableStateListOf(PreviewFixtures.github.id) },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = PreviewFixtures.accountCounts,
            accountsLayout = AccountsLayout.Cards,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun CardsWithAccountsAndGroups() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Success(PreviewFixtures.accounts),
            accountRealtimeData = PreviewFixtures.rememberRealtimeData(),
            selectedAccounts = remember { mutableStateListOf<UUID>() },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = PreviewFixtures.groups,
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = PreviewFixtures.groupedAccountCounts,
            accountsLayout = AccountsLayout.Cards,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun CompactWithAccounts() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Success(PreviewFixtures.accounts),
            accountRealtimeData = PreviewFixtures.rememberRealtimeData(),
            selectedAccounts = remember { mutableStateListOf<UUID>() },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = PreviewFixtures.accountCounts,
            accountsLayout = AccountsLayout.Compact,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun CompactAccountSelection() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Success(PreviewFixtures.accounts),
            accountRealtimeData = PreviewFixtures.rememberRealtimeData(),
            selectedAccounts = remember { mutableStateListOf(PreviewFixtures.github.id) },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = PreviewFixtures.accountCounts,
            accountsLayout = AccountsLayout.Compact,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun CompactWithAccountsAndGroups() = ScreenshotSurface {
        HomeScreen(
            onAddAccountNavigate = {},
            onMoreMenuNavigate = {},
            onAccountSelect = {},
            onCancelAccountSelection = {},
            onDeleteSelectedAccounts = {},
            onExportSelectedAccounts = {},
            onAccountEdit = {},
            onAccountCounterIncrease = {},
            onAccountCopyCode = { _, _, _ -> },
            state = HomeScreenState.Success(PreviewFixtures.accounts),
            accountRealtimeData = PreviewFixtures.rememberRealtimeData(),
            selectedAccounts = remember { mutableStateListOf<UUID>() },
            activeAccountsSort = AccountsSort.DEFAULT,
            onActiveSortChange = {},
            groups = PreviewFixtures.groups,
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            accountCounts = PreviewFixtures.groupedAccountCounts,
            accountsLayout = AccountsLayout.Compact,
            showCodesByDefault = false,
            modifier = Modifier.fillMaxSize(),
            showScanButton = false
        )
    }
}
