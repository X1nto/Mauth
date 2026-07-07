package com.xinto.mauth.ui.screen.font

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.FontSetting
import com.xinto.mauth.ui.screen.settings.component.SettingsGroup
import com.xinto.mauth.ui.screen.settings.component.SettingsRadioItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FontScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: FontViewModel = koinViewModel()
    val font by viewModel.font.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.font_title)) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
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
            SettingsGroup {
                FontSetting.entries.forEachIndexed { i, it ->
                    SettingsRadioItem(
                        selected = font == it,
                        title = {
                            val textRes = remember(it) {
                                when (it) {
                                    FontSetting.Roboto -> R.string.font_font_roboto
                                    FontSetting.GoogleSans -> R.string.font_font_google_sans
                                }
                            }
                            Text(stringResource(textRes))
                        },
                        onClick = { viewModel.updateFont(it) },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = i,
                            count = FontSetting.entries.count()
                        )
                    )
                }
            }
        }
    }
}