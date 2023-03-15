package com.xinto.mauth.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.settings.usecase.GetSecureModeUsecase
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.screen.account.AddAccountScreen
import com.xinto.mauth.ui.screen.account.EditAccountScreen
import com.xinto.mauth.ui.screen.home.HomeScreen
import com.xinto.mauth.ui.screen.qrscan.QrScanScreen
import com.xinto.mauth.ui.screen.settings.SettingsScreen
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.taxi.Taxi
import com.xinto.taxi.rememberBackstackNavigator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val secureMode: GetSecureModeUsecase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secureMode()
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

        setContent {
            MauthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main()
                }
            }
        }
    }
}

@Composable
fun Main() {
    val navigator = rememberBackstackNavigator<MauthDestination>(MauthDestination.Home)
    Taxi(
        navigator = navigator,
        transitionSpec = {
            when {
                targetState.isFullscreenDialog -> {
                    slideIntoContainer(
                        towards = AnimatedContentScope.SlideDirection.Up,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) with fadeOut()
                }
                initialState.isFullscreenDialog -> {
                    fadeIn() with slideOutOfContainer(
                        towards = AnimatedContentScope.SlideDirection.Down,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
                }
                else -> fadeIn() with fadeOut()
            }
        }
    ) { screen ->
        when (screen) {
            is MauthDestination.Home -> {
                HomeScreen(
                    onAddAccountManually = {
                        navigator.push(MauthDestination.AddAccount(DomainAccountInfo.DEFAULT))
                    },
                    onAddAccountViaScanning = {
                        navigator.push(MauthDestination.QrScanner)
                    },
                    onAddAccountFromImage = {
                        navigator.push(MauthDestination.AddAccount(it))
                    },
                    onSettingsClick = {
                        navigator.push(MauthDestination.Settings)
                    },
                    onAccountEdit = {
                        navigator.push(MauthDestination.EditAccount(it))
                    }
                )
            }
            is MauthDestination.QrScanner -> {
                QrScanScreen(
                    onBack = {
                        navigator.pop()
                    },
                    onScan = {
                        navigator.replace(MauthDestination.AddAccount(it))
                    }
                )
            }
            is MauthDestination.Settings -> {
                SettingsScreen(
                    onBack = {
                        navigator.pop()
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
        }
    }
}