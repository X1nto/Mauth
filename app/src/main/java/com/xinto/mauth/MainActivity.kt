package com.xinto.mauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.screen.AddAccountScreen
import com.xinto.mauth.ui.screen.HomeScreen
import com.xinto.mauth.ui.screen.QrScannerScreen
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import com.xinto.taxi.Taxi
import com.xinto.taxi.rememberBackstackNavigator
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                HomeScreen(navigator)
            }
            is MauthDestination.QrScanner -> {
                QrScannerScreen(navigator)
            }
            is MauthDestination.Settings -> {

            }
            is MauthDestination.AddAccount -> {
                val viewModel: AddAccountViewModel = getViewModel { parametersOf(screen.params) }
                AddAccountScreen(
                    navigator = navigator,
                    viewModel = viewModel
                )
            }
        }
    }
}