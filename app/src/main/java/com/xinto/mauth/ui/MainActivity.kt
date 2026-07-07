package com.xinto.mauth.ui

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.core.os.BundleCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import com.xinto.mauth.core.settings.model.ThemeSetting
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.SettingsRepository
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.screen.about.AboutScreen
import com.xinto.mauth.ui.screen.account.AddAccountScreen
import com.xinto.mauth.ui.screen.account.EditAccountScreen
import com.xinto.mauth.ui.screen.auth.AuthScreen
import com.xinto.mauth.ui.screen.export.ExportScreen
import com.xinto.mauth.ui.screen.groups.GroupsScreen
import com.xinto.mauth.ui.screen.home.HomeScreen
import com.xinto.mauth.ui.screen.pinremove.PinRemoveScreen
import com.xinto.mauth.ui.screen.pinsetup.PinSetupScreen
import com.xinto.mauth.ui.screen.qrscan.QrScanScreen
import com.xinto.mauth.ui.screen.settings.SettingsScreen
import com.xinto.mauth.ui.screen.theme.ThemeScreen
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.util.launchInLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val settings: SettingsRepository by inject()
    private val otp: OtpRepository by inject()
    private val accounts: AccountRepository by inject()
    private val auth: AuthRepository by inject()

    private val lockOnResume: StateFlow<Boolean> = settings.getLockOnResume()

    private lateinit var navigator: MauthNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        settings.getTheme()
            .launchInLifecycle(lifecycle) {
                val systemBarStyle = when (it) {
                    ThemeSetting.System -> SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
                    ThemeSetting.Dark -> SystemBarStyle.dark(Color.TRANSPARENT)
                    ThemeSetting.Light -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(systemBarStyle, systemBarStyle)
            }

        settings.getSecureMode()
            .launchInLifecycle(lifecycle) {
                if (it) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

        val backStack = savedInstanceState
            ?.let { BundleCompat.getParcelableArrayList(it, KEY_BACKSTACK, MauthDestination::class.java) }
            ?.toMutableStateList()
            ?: mutableStateListOf(
                if (runBlocking { auth.isProtected() }) MauthDestination.Auth() else MauthDestination.Home
            )
        navigator = MauthNavigator(backStack) {
            runBlocking { auth.isProtected() }
        }

        setContent {
            val theme by settings.getTheme().collectAsStateWithLifecycle()
            val color by settings.getColor().collectAsStateWithLifecycle()
            val font by settings.getFont().collectAsStateWithLifecycle()
            MauthTheme(
                theme = theme,
                color = color,
                font = font
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var handledIntentData by rememberSaveable { mutableStateOf<String?>(null) }
                    LaunchedEffect(intent.data) {
                        val data = intent.data?.toString()
                        if (data == null || data == handledIntentData) return@LaunchedEffect
                        val accountInfo = when (val parseResult = otp.parseUri(data)) {
                            is OtpUriParserResult.Success -> with(accounts) { parseResult.data.toAccountInfo() }
                            else -> null
                        }
                        handledIntentData = data
                        if (accountInfo != null) {
                            navigator.navigate(MauthDestination.AddAccount(accountInfo))
                        }
                    }

                    NavDisplay(
                        backStack = navigator.backStack,
                        modifier = Modifier.fillMaxSize(),
                        onBack = { navigator.pop() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                        transitionSpec = {
                            val target = targetState.entries.lastOrNull()?.contentKey as? MauthDestination
                            val initial = initialState.entries.lastOrNull()?.contentKey as? MauthDestination
                            when {
                                target?.isFullscreenDialog == true -> {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ) togetherWith fadeOut()
                                }
                                initial is MauthDestination.Auth -> {
                                    fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ) togetherWith fadeOut() + slideOut {
                                        IntOffset(0, -100)
                                    }
                                }
                                else -> {
                                    fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ) togetherWith fadeOut() + scaleOut(
                                        targetScale = 1.1f
                                    )
                                }
                            }
                        },
                        popTransitionSpec = {
                            val popped = initialState.entries.lastOrNull()?.contentKey as? MauthDestination
                            if (popped?.isFullscreenDialog == true) {
                                fadeIn() togetherWith slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessVeryLow
                                    )
                                )
                            } else {
                                fadeIn() + scaleIn(
                                    initialScale = 1.1f
                                ) togetherWith fadeOut() + scaleOut(
                                    targetScale = 0.9f
                                )
                            }
                        },
                        predictivePopTransitionSpec = {
                            val popped = initialState.entries.lastOrNull()?.contentKey as? MauthDestination
                            if (popped?.isFullscreenDialog == true) {
                                fadeIn() togetherWith slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessVeryLow
                                    )
                                )
                            } else {
                                fadeIn() + scaleIn(
                                    initialScale = 1.1f
                                ) togetherWith fadeOut() + scaleOut(
                                    targetScale = 0.9f
                                )
                            }
                        },
                        entryProvider = entryProvider {
                            entry<MauthDestination.Auth> { key ->
                                AuthScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onAuthSuccess = {
                                        when {
                                            key.nextDestination != null -> navigator.replaceLast(key.nextDestination)
                                            navigator.backStack.size > 1 -> navigator.pop()
                                            else -> navigator.replaceAll(MauthDestination.Home)
                                        }
                                    },
                                    onBackPress = if (key.nextDestination == null) null else { ->
                                        navigator.pop()
                                    }
                                )
                            }
                            entry<MauthDestination.Home> {
                                HomeScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onAddAccountManually = { groupId ->
                                        navigator.navigate(
                                            MauthDestination.AddAccount(DomainAccountInfo.new().copy(groupId = groupId))
                                        )
                                    },
                                    onAddAccountViaScanning = {
                                        navigator.navigate(MauthDestination.QrScanner)
                                    },
                                    onAddAccountFromImage = {
                                        navigator.navigate(MauthDestination.AddAccount(it))
                                    },
                                    onAccountEdit = {
                                        navigator.navigate(MauthDestination.EditAccount(it))
                                    },
                                    onSettingsNavigate = {
                                        navigator.navigate(MauthDestination.Settings)
                                    },
                                    onExportNavigate = { accounts ->
                                        navigator.navigateSecure(MauthDestination.Export(accounts))
                                    },
                                    onAboutNavigate = {
                                        navigator.navigate(MauthDestination.About)
                                    },
                                    onManageGroups = {
                                        navigator.navigate(MauthDestination.Groups)
                                    }
                                )
                            }
                            entry<MauthDestination.QrScanner> {
                                QrScanScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onBack = navigator::pop,
                                    onScan = {
                                        navigator.replaceLast(MauthDestination.AddAccount(it))
                                    }
                                )
                            }
                            entry<MauthDestination.Settings> {
                                SettingsScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onBack = navigator::pop,
                                    onSetupPinCode = {
                                        navigator.navigate(MauthDestination.PinSetup)
                                    },
                                    onDisablePinCode = {
                                        navigator.navigate(MauthDestination.PinRemove)
                                    },
                                    onThemeNavigate = {
                                        navigator.navigate(MauthDestination.Theme)
                                    }
                                )
                            }
                            entry<MauthDestination.About> {
                                AboutScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onBack = navigator::pop
                                )
                            }
                            entry<MauthDestination.Groups> {
                                GroupsScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onBack = navigator::pop,
                                    onAddAccount = { groupId ->
                                        navigator.navigate(
                                            MauthDestination.AddAccount(DomainAccountInfo.new().copy(groupId = groupId))
                                        )
                                    }
                                )
                            }
                            entry<MauthDestination.AddAccount> { key ->
                                AddAccountScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    prefilled = key.params,
                                    onExit = navigator::pop
                                )
                            }
                            entry<MauthDestination.EditAccount> { key ->
                                EditAccountScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    id = key.id,
                                    onExit = navigator::pop
                                )
                            }
                            entry<MauthDestination.PinSetup> {
                                PinSetupScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onExit = navigator::pop
                                )
                            }
                            entry<MauthDestination.PinRemove> {
                                PinRemoveScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onExit = navigator::pop
                                )
                            }
                            entry<MauthDestination.Theme> {
                                ThemeScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onExit = navigator::pop
                                )
                            }
                            entry<MauthDestination.Export> { key ->
                                ExportScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    accounts = key.accounts,
                                    onBackNavigate = navigator::pop
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if (lockOnResume.value &&
            !isChangingConfigurations &&
            navigator.backStack.lastOrNull() !is MauthDestination.Auth &&
            runBlocking { auth.isProtected() }
        ) {
            navigator.navigate(MauthDestination.Auth())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_BACKSTACK, ArrayList(navigator.backStack))
    }

    private companion object {
        const val KEY_BACKSTACK = "backstack"
    }
}