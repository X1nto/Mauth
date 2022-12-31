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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.otp.parser.OtpUriParserError
import com.xinto.mauth.otp.parser.OtpUriParserImpl
import com.xinto.mauth.otp.parser.OtpUriParserResult
import com.xinto.mauth.ui.navigation.MauthDestination
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

        // Parse Account to add, if there is one to add
        var accountToAdd: DomainAccountInfo? = null
        var parseError: OtpUriParserError? = null


        if (intent?.scheme == "otpauth" && intent?.dataString != null) {
            val parser = OtpUriParserImpl()

            accountToAdd = when (val result = parser.parseOtpUri(intent?.dataString!!)) {
                is OtpUriParserResult.Success ->
                    DomainAccountInfo(
                        id = null,
                        icon = null,
                        label = result.data.label,
                        issuer = result.data.issuer,
                        secret = result.data.secret,
                        algorithm = result.data.algorithm,
                        type = result.data.type,
                        digits = result.data.digits,
                        counter = result.data.counter ?: 0,
                        period = result.data.period ?: 30,
                    )
                is OtpUriParserResult.Failure -> {
                    parseError = result.error
                    null
                }
            }
        }

        setContent {
            MauthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // If there was an error parsing the URI, display an alert dialog
                    if (parseError != null) {
                        val showErrorDialog = remember { mutableStateOf(true) }
                        if(showErrorDialog.value)
                            AlertDialog(
                                onDismissRequest = { showErrorDialog.value = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = { showErrorDialog.value = false }) {
                                        Text(stringResource(android.R.string.ok))
                                    }
                                },
                                title = {
                                    Text(stringResource(R.string.urihandler_parseerror_dialog_title))
                                },
                                text = {
                                    Text(String.format(
                                        "%s: %s",
                                        stringResource(R.string.urihandler_parseerror_dialog_body),
                                        parseError.toString()
                                    ))
                                },
                            )
                    }
                    Main(accountToAdd)
                }
            }
        }
    }
}

@Composable
fun Main(accountToAdd: DomainAccountInfo?) {
    val navigator = rememberBackstackNavigator<MauthDestination>(MauthDestination.Home)

    if (accountToAdd != null)
        navigator.push(MauthDestination.AddAccount(accountToAdd))

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
