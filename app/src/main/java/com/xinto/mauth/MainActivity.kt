package com.xinto.mauth

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.screen.*
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.ui.viewmodel.MainViewModel
import com.xinto.taxi.Taxi
import com.xinto.taxi.rememberBackstackNavigator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // The app is started - wouldn't run after a configuration change
            viewModel.handleIntentData(intent)
        }

        viewModel.privateMode
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
                    val navigator =
                        rememberBackstackNavigator<MauthDestination>(MauthDestination.Home)

                    Main(navigator)

                    HandleUriResult(viewModel, navigator)
                }
            }
        }
    }
}

@Composable
fun Main(navigator: MauthNavigator) {
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
                HomeScreen(navigator)
            }
            is MauthDestination.QrScanner -> {
                QrScannerScreen(navigator)
            }
            is MauthDestination.Settings -> {
                SettingsScreen(navigator)
            }
            is MauthDestination.AddAccount -> {
                AddAccountScreen(navigator = navigator, accountInfo = screen.params)
            }
            is MauthDestination.EditAccount -> {
                EditAccountScreen(navigator = navigator, accountId = screen.id)
            }
        }
    }
}

@Composable
private fun HandleUriResult(
    mainViewModel: MainViewModel,
    navigator: MauthNavigator,
) {
    // collect opt URI parse result
    val uriData by mainViewModel.optUri
    LaunchedEffect(uriData) {
        uriData?.let { data ->
            navigator.push(MauthDestination.AddAccount(data))
            mainViewModel.onUriHandled()
        }
    }
}
