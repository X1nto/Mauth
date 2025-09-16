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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.SettingsRepository
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.screen.about.AboutScreen
import com.xinto.mauth.ui.screen.account.AddAccountScreen
import com.xinto.mauth.ui.screen.account.EditAccountScreen
import com.xinto.mauth.ui.screen.auth.AuthScreen
import com.xinto.mauth.ui.screen.export.ExportScreen
import com.xinto.mauth.ui.screen.home.HomeScreen
import com.xinto.mauth.ui.screen.pinremove.PinRemoveScreen
import com.xinto.mauth.ui.screen.pinsetup.PinSetupScreen
import com.xinto.mauth.ui.screen.qrscan.QrScanScreen
import com.xinto.mauth.ui.screen.settings.SettingsScreen
import com.xinto.mauth.ui.screen.theme.ThemeScreen
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.util.launchInLifecycle
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavAction
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController
import dev.olshevski.navigation.reimagined.replaceAll
import dev.olshevski.navigation.reimagined.replaceLast
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val settings: SettingsRepository by inject()
    private val otp: OtpRepository by inject()
    private val accounts: AccountRepository by inject()
    private val auth: AuthRepository by inject()

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


        val initialScreen = runBlocking {
            if (auth.isProtected()) {
                MauthDestination.Auth()
            } else {
                MauthDestination.Home
            }
        }

        setContent {
            val theme by settings.getTheme().collectAsStateWithLifecycle(initialValue = ThemeSetting.DEFAULT)
            val color by settings.getColor().collectAsStateWithLifecycle(initialValue = ColorSetting.DEFAULT)
            MauthTheme(
                theme = theme,
                color = color
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navigator = rememberNavController(initialScreen)

                    LaunchedEffect(intent.data) {
                        val accountInfo = when (val parseResult = otp.parseUri(intent.data.toString())) {
                            is OtpUriParserResult.Success -> with(accounts) {
                                parseResult.data.toAccountInfo()
                            }
                            else -> null
                        }
                        if (accountInfo != null) {
                            navigator.navigate(MauthDestination.AddAccount(accountInfo))
                        }
                    }

                    AnimatedNavHost(
                        controller = navigator,
                        transitionSpec = { action, initial, target ->
                            when {
                                target.isFullscreenDialog -> {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ) togetherWith fadeOut()
                                }
                                initial.isFullscreenDialog -> {
                                    fadeIn() togetherWith slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessVeryLow
                                        )
                                    )
                                }
                                initial is MauthDestination.Auth && action !is NavAction.Pop -> {
                                    fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ) togetherWith fadeOut() + slideOut {
                                        IntOffset(0, -100)
                                    }
                                }
                                else -> when (action) {
                                    NavAction.Navigate -> {
                                        fadeIn() + scaleIn(
                                            initialScale = 0.9f
                                        ) togetherWith fadeOut() + scaleOut(
                                            targetScale = 1.1f
                                        )
                                    }
                                    NavAction.Pop -> {
                                        fadeIn() + scaleIn(
                                            initialScale = 1.1f
                                        ) togetherWith fadeOut() + scaleOut(
                                            targetScale = 0.9f
                                        )
                                    }
                                    else -> fadeIn() togetherWith fadeOut()
                                }
                            }
                        }
                    ) { screen ->
                        when (screen) {
                            is MauthDestination.Auth -> {
                                AuthScreen(
                                    onAuthSuccess = {
                                        if (screen.nextDestination != null) {
                                            navigator.replaceLast(screen.nextDestination)
                                        } else {
                                            navigator.replaceAll(MauthDestination.Home)
                                        }
                                    },
                                    onBackPress = if (screen.nextDestination == null) null else { ->
                                        navigator.pop()
                                    }
                                )
                            }
                            is MauthDestination.Home -> {
                                HomeScreen(
                                    onAddAccountManually = {
                                        navigator.navigate(
                                            MauthDestination.AddAccount(DomainAccountInfo.new())
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
                                    }
                                )
                            }
                            is MauthDestination.QrScanner -> {
                                QrScanScreen(
                                    onBack = navigator::pop,
                                    onScan = {
                                        navigator.replaceLast(MauthDestination.AddAccount(it))
                                    }
                                )
                            }
                            is MauthDestination.Settings -> {
                                SettingsScreen(
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
                            is MauthDestination.About -> {
                                AboutScreen(onBack = navigator::pop)
                            }
                            is MauthDestination.AddAccount -> {
                                AddAccountScreen(
                                    prefilled = screen.params,
                                    onExit = navigator::pop
                                )
                            }
                            is MauthDestination.EditAccount -> {
                                EditAccountScreen(
                                    id = screen.id,
                                    onExit = navigator::pop
                                )
                            }
                            is MauthDestination.PinSetup -> {
                                PinSetupScreen(onExit = navigator::pop)
                            }
                            is MauthDestination.PinRemove -> {
                                PinRemoveScreen(onExit = navigator::pop)
                            }
                            is MauthDestination.Theme -> {
                                ThemeScreen(onExit = navigator::pop)
                            }
                            is MauthDestination.Export -> {
                                ExportScreen(
                                    accounts = screen.accounts,
                                    onBackNavigate = navigator::pop
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun NavController<MauthDestination>.navigateSecure(destination: MauthDestination) {
        val isProtected = runBlocking { auth.isProtected() }
        if (isProtected) {
            navigate(MauthDestination.Auth(nextDestination = destination))
        } else {
            navigate(destination)
        }
    }
}