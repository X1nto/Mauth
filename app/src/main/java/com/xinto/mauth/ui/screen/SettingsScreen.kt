package com.xinto.mauth.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.SwitchSettingsItem
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    navigator: MauthNavigator
) {
    val viewModel: SettingsViewModel = koinViewModel()
    BackHandler {
        navigator.pop()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title))
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SwitchSettingsItem(
                    onCheckedChange = viewModel::updatePrivateMode,
                    checked = viewModel.privateMode,
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