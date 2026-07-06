package com.xinto.mauth.ui.screen.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.rememberBiometricHandler
import com.xinto.mauth.ui.component.rememberBiometricPromptData
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.screen.settings.component.SettingsGroup
import com.xinto.mauth.ui.screen.settings.component.SettingsNavigateItem
import com.xinto.mauth.ui.screen.settings.component.SettingsSwitchItem
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSetupPinCode: () -> Unit,
    onDisablePinCode: () -> Unit,
    onThemeNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val secureMode by viewModel.secureMode.collectAsStateWithLifecycle()
    val lockOnResume by viewModel.lockOnResume.collectAsStateWithLifecycle()
    val pinLock by viewModel.pinLock.collectAsStateWithLifecycle()
    val biometrics by viewModel.biometrics.collectAsStateWithLifecycle()

    val biometricHandler = rememberBiometricHandler(
        onAuthSuccess = viewModel::toggleBiometrics
    )
    val setupPromptData = rememberBiometricPromptData(
        title = stringResource(R.string.settings_biometrics_setup_title),
        negativeButtonText = stringResource(R.string.settings_biometrics_setup_cancel)
    )
    val disablePromptData = rememberBiometricPromptData(
        title = stringResource(R.string.settings_biometrics_disable_title),
        negativeButtonText = stringResource(R.string.settings_biometrics_disable_cancel)
    )

    BackHandler(onBack = onBack)
    SettingsScreen(
        modifier = modifier,
        onBack = onBack,
        secureMode = secureMode,
        onSecureModeChange = viewModel::updateSecureMode,
        pinCode = pinLock,
        onPinCodeChange = {
            if (it) {
                onSetupPinCode()
            } else {
                onDisablePinCode()
            }
        },
        lockOnResume = lockOnResume,
        onLockOnResumeChange = viewModel::updateLockOnResume,
        showBiometrics = biometricHandler.canUseBiometrics(),
        biometrics = biometrics,
        onBiometricsChange = {
            val promptData = if (it) setupPromptData else disablePromptData
            biometricHandler.requestBiometrics(promptData)
        },
        onThemeNavigate = onThemeNavigate
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    secureMode: Boolean,
    onSecureModeChange: (Boolean) -> Unit,
    pinCode: Boolean,
    onPinCodeChange: (Boolean) -> Unit,
    lockOnResume: Boolean,
    onLockOnResumeChange: (Boolean) -> Unit,
    showBiometrics: Boolean,
    biometrics: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    onThemeNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .wrapContentWidth()
                .widthIn(max = 600.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup(header = { Text(stringResource(R.string.settings_category_security)) }) {
                val count = if (showBiometrics) 4 else 3
                SettingsSwitchItem(
                    onCheckedChange = onSecureModeChange,
                    checked = secureMode,
                    title = { Text(stringResource(R.string.settings_prefs_securemode)) },
                    description = { Text(stringResource(R.string.settings_prefs_securemode_description)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_security),
                            contentDescription = null
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = count)
                )
                SettingsSwitchItem(
                    onCheckedChange = onPinCodeChange,
                    checked = pinCode,
                    title = { Text(stringResource(R.string.settings_prefs_pincode)) },
                    description = { Text(stringResource(R.string.settings_prefs_pincode_description)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_pin),
                            contentDescription = null
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = count)
                )
                if (showBiometrics) {
                    SettingsSwitchItem(
                        onCheckedChange = onBiometricsChange,
                        checked = biometrics,
                        title = { Text(stringResource(R.string.settings_prefs_biometrics)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_fingerprint),
                                contentDescription = null
                            )
                        },
                        enabled = pinCode,
                        shapes = ListItemDefaults.segmentedShapes(index = 2, count = count)
                    )
                }
                SettingsSwitchItem(
                    onCheckedChange = onLockOnResumeChange,
                    checked = pinCode && lockOnResume,
                    title = { Text(stringResource(R.string.settings_prefs_autolock)) },
                    description = { Text(stringResource(R.string.settings_prefs_autolock_description)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock_reset),
                            contentDescription = null
                        )
                    },
                    enabled = pinCode,
                    shapes = ListItemDefaults.segmentedShapes(index = if (showBiometrics) 3 else 2, count = count)
                )
            }
            SettingsGroup(header = { Text(stringResource(R.string.settings_category_appearance)) }) {
                SettingsNavigateItem(
                    onClick = onThemeNavigate,
                    title = { Text(stringResource(R.string.settings_prefs_theme)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_brush),
                            contentDescription = null
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = 1,
                        defaultShapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
                    )
                )
            }
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun SettingsScreen_Default_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                secureMode = false,
                onSecureModeChange = {},
                pinCode = false,
                onPinCodeChange = {},
                lockOnResume = false,
                onLockOnResumeChange = {},
                showBiometrics = false,
                biometrics = false,
                onBiometricsChange = {},
                onThemeNavigate = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun SettingsScreen_AllEnabled_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                secureMode = true,
                onSecureModeChange = {},
                pinCode = true,
                onPinCodeChange = {},
                lockOnResume = true,
                onLockOnResumeChange = {},
                showBiometrics = true,
                biometrics = true,
                onBiometricsChange = {},
                onThemeNavigate = {}
            )
        }
    }
}