package com.xinto.mauth.ui.screen.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.domain.settings.model.ColorScheme
import com.xinto.mauth.domain.settings.model.Theme
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.screen.theme.component.ThemeColorCard
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.ui.screen.settings.labelRes
import org.koin.androidx.compose.koinViewModel

@Composable
fun ThemeScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ThemeViewModel = koinViewModel()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val color by viewModel.color.collectAsStateWithLifecycle()
    ThemeScreen(
        modifier = modifier,
        onBack = onExit,
        theme = theme,
        onThemeChange = viewModel::updateTheme,
        color = color,
        onColorChange = viewModel::updateColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    onBack: () -> Unit,
    theme: Theme,
    onThemeChange: (Theme) -> Unit,
    color: ColorScheme,
    onColorChange: (ColorScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.theme_title))
                },
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
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = GridCells.Adaptive(175.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    Theme.entries.forEachIndexed { i, it ->
                        SegmentedButton(
                            selected = theme == it,
                            onClick = { onThemeChange(it) },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = Theme.entries.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(
                                    active = theme == it,
                                    inactiveContent = {
                                        val drawableRes = when (it) {
                                            Theme.System -> R.drawable.ic_contrast
                                            Theme.Dark -> R.drawable.ic_moon
                                            Theme.Light -> R.drawable.ic_sun
                                        }
                                        Icon(
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                            painter = painterResource(drawableRes),
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        ) {
                            Text(stringResource(it.labelRes))
                        }
                    }
                }
            }
            items(ColorScheme.validEntries) { colorSetting ->
                MauthTheme(
                    theme = theme,
                    color = colorSetting
                ) {
                    ThemeColorCard(
                        onClick = {
                            onColorChange(colorSetting)
                        },
                        name = {
                            Text(stringResource(colorSetting.labelRes))
                        },
                        selected = color == colorSetting
                    )
                }
            }
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun ThemeScreen_System_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThemeScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                theme = Theme.System,
                onThemeChange = {},
                color = ColorScheme.MothPurple,
                onColorChange = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun ThemeScreen_BlueSelected_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThemeScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                theme = Theme.System,
                onThemeChange = {},
                color = ColorScheme.BlueberryBlue,
                onColorChange = {}
            )
        }
    }
}