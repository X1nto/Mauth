package com.xinto.mauth.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.OtpRepository
import com.xinto.mauth.domain.SettingsRepository
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.screen.account.AddAccountScreen
import com.xinto.mauth.ui.screen.account.EditAccountScreen
import com.xinto.mauth.ui.screen.auth.AuthScreen
import com.xinto.mauth.ui.screen.home.HomeScreen
import com.xinto.mauth.ui.screen.pinremove.PinRemoveScreen
import com.xinto.mauth.ui.screen.pinsetup.PinSetupScreen
import com.xinto.mauth.ui.screen.qrscan.QrScanScreen
import com.xinto.mauth.ui.screen.settings.SettingsScreen
import com.xinto.mauth.ui.theme.MauthTheme
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController
import dev.olshevski.navigation.reimagined.replaceAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val settings: SettingsRepository by inject()
    private val otp: OtpRepository by inject()
    private val auth: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        settings.getSecureMode()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            .launchIn(lifecycleScope)

        val initialScreen = runBlocking {
            if (auth.isProtected()) {
                MauthDestination.Auth
            } else {
                MauthDestination.Home
            }
        }

        setContent {
            MauthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navigator = rememberNavController(initialScreen)

                    LaunchedEffect(intent.data) {
                        val accountInfo = otp.parseUriToAccountInfo(intent.data.toString())
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
                                    fadeIn() togetherWith  slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessVeryLow
                                        )
                                    )
                                }
                                else -> fadeIn() togetherWith fadeOut()
                            }
                        }
                    ) { screen ->
                        when (screen) {
                            is MauthDestination.Auth -> {
                                AuthScreen(
                                    onAuthSuccess = {
                                        navigator.replaceAll(MauthDestination.Home)
                                    }
                                )
                            }
                            is MauthDestination.Home -> {
                                HomeScreen(
                                    onAddAccountManually = {
                                        navigator.navigate(MauthDestination.AddAccount(
                                            DomainAccountInfo.DEFAULT))
                                    },
                                    onAddAccountViaScanning = {
                                        navigator.navigate(MauthDestination.QrScanner)
                                    },
                                    onAddAccountFromImage = {
                                        navigator.navigate(MauthDestination.AddAccount(it))
                                    },
                                    onSettingsClick = {
                                        navigator.navigate(MauthDestination.Settings)
                                    },
                                    onAccountEdit = {
                                        navigator.navigate(MauthDestination.EditAccount(it))
                                    }
                                )
                            }
                            is MauthDestination.QrScanner -> {
                                QrScanScreen(
                                    onBack = {
                                        navigator.pop()
                                    },
                                    onScan = {
                                        navigator.replaceAll(MauthDestination.AddAccount(it))
                                    }
                                )
                            }
                            is MauthDestination.Settings -> {
                                SettingsScreen(
                                    onBack = {
                                        navigator.pop()
                                    },
                                    onSetupPinCode = {
                                        navigator.navigate(MauthDestination.PinSetup)
                                    },
                                    onDisablePinCode = {
                                        navigator.navigate(MauthDestination.PinRemove)
                                    }
                                )
                            }
                            is MauthDestination.AddAccount -> {
                                AddAccountScreen(
                                    prefilled = screen.params,
                                    onExit = {
                                        navigator.pop()
                                    }
                                )
                            }
                            is MauthDestination.EditAccount -> {
                                EditAccountScreen(
                                    id = screen.id,
                                    onExit = {
                                        navigator.pop()
                                    }
                                )
                            }
                            is MauthDestination.PinSetup -> {
                                PinSetupScreen(
                                    onExit = {
                                        navigator.pop()
                                    }
                                )
                            }
                            is MauthDestination.PinRemove -> {
                                PinRemoveScreen(
                                    onExit = {
                                        navigator.pop()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}