package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.core.settings.model.SortSetting
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
            activeSortSetting = SortSetting.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            modifier = Modifier.fillMaxSize(),
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun WithAccounts() = ScreenshotSurface {
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
            activeSortSetting = SortSetting.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            modifier = Modifier.fillMaxSize(),
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun AccountSelection() = ScreenshotSurface {
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
            activeSortSetting = SortSetting.DEFAULT,
            onActiveSortChange = {},
            groups = persistentListOf(),
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            modifier = Modifier.fillMaxSize(),
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun WithAccountsAndGroups() = ScreenshotSurface {
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
            activeSortSetting = SortSetting.DEFAULT,
            onActiveSortChange = {},
            groups = PreviewFixtures.groups,
            activeGroup = GroupFilter.All,
            onActiveGroupChange = {},
            onCreateGroupClick = {},
            onGroupSelectedClick = {},
            searchAccounts = persistentListOf(),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
