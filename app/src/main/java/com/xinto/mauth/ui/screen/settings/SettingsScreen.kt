package com.xinto.mauth.ui.screen.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.screen.settings.component.SettingsSwitch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val secureMode by viewModel.secureMode.collectAsStateWithLifecycle()
    SettingsScreen(
        onBack = onBack,
        secureMode = secureMode,
        onSecureModeChange = viewModel::updateSecureMode
    )
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    secureMode: Boolean,
    onSecureModeChange: (Boolean) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSwitch(
                    onCheckedChange = onSecureModeChange,
                    checked = secureMode,
                    title = {
                        Text(stringResource(R.string.settings_prefs_securemode))
                    },
                    description = {
                        Text(stringResource(R.string.settings_prefs_securemode_description))
                    }
                )
            }
        }
    }
}