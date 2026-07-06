package com.xinto.mauth.screenshot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.ui.screen.account.AccountForm
import com.xinto.mauth.ui.screen.account.AccountScreen
import com.xinto.mauth.ui.screen.account.AccountScreenState
import com.xinto.mauth.ui.screen.auth.AuthScreen
import com.xinto.mauth.ui.screen.about.AboutScreen
import com.xinto.mauth.ui.screen.home.HomeScreen
import com.xinto.mauth.ui.screen.home.HomeScreenState
import com.xinto.mauth.ui.screen.settings.SettingsScreen
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.domain.group.model.GroupFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

data class StoreScreenshot(
    val order: Int,
    val id: String,
) {
    val fileName: String get() = "$order.png"
    override fun toString(): String = "%02d_%s".format(order, id)
}

val storeScreenshots: List<StoreScreenshot> = listOf(
    StoreScreenshot(1, "auth_pin"),
    StoreScreenshot(2, "home_accounts"),
    StoreScreenshot(3, "account_add"),
    StoreScreenshot(4, "home_selection"),
    StoreScreenshot(5, "settings"),
    StoreScreenshot(6, "about"),
)

@Composable
fun StoreScreenshotContent(id: String) {
    when (id) {
        "auth_pin" -> AuthPinFixture()
        "account_add" -> AccountAddFixture()
        "home_accounts" -> HomeAccountsFixture(selected = emptyList(), groups = StoreFixtures.groups)
        "home_selection" -> HomeAccountsFixture(selected = listOf(StoreFixtures.discordAccount.id, StoreFixtures.hotpAccount.id))
        "settings" -> SettingsFixture()
        "about" -> AboutFixture()
        else -> error("Unknown screenshot id: $id")
    }
}

@Composable
private fun AuthPinFixture() {
    AuthScreen(
        code = "",
        onNumberAdd = {},
        onNumberDelete = {},
        onClear = {},
        showFingerprint = true,
        onFingerprintClick = {},
        onBackPress = null,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun HomeAccountsFixture(
    selected: List<UUID>,
    groups: ImmutableList<DomainGroup> = persistentListOf(),
) {
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
        state = HomeScreenState.Success(StoreFixtures.sampleAccounts),
        accountRealtimeData = remember { StoreFixtures.sampleRealtimeData },
        selectedAccounts = remember { selected.toMutableStateList() },
        activeSortSetting = SortSetting.DEFAULT,
        onActiveSortChange = {},
        groups = groups,
        activeGroup = GroupFilter.All,
        onActiveGroupChange = {},
        onCreateGroupClick = {},
        onGroupSelectedClick = {},
        searchAccounts = persistentListOf(),
        modifier = Modifier.fillMaxSize(),
        showScanButton = false
    )
}

@Composable
private fun AccountAddFixture() {
    val form = remember {
        AccountForm(
            initial = StoreFixtures.addAccountInfo,
            groups = MutableStateFlow(emptyList()),
            onCreateGroup = { _, _ -> UUID.randomUUID() },
        )
    }
    AccountScreen(
        title = "Add an account",
        state = AccountScreenState.Success(form),
        onSave = {},
        onExit = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun SettingsFixture() {
    SettingsScreen(
        onBack = {},
        secureMode = false,
        onSecureModeChange = {},
        pinCode = true,
        onPinCodeChange = {},
        lockOnResume = false,
        onLockOnResumeChange = {},
        showBiometrics = true,
        biometrics = true,
        onBiometricsChange = {},
        onThemeNavigate = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun AboutFixture() {
    AboutScreen(
        modifier = Modifier.fillMaxSize(),
        onBack = {},
    )
}
