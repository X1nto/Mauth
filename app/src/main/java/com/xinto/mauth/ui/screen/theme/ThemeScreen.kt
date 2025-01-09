package com.xinto.mauth.ui.screen.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import com.xinto.mauth.ui.screen.theme.component.ThemeColorCard
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun ThemeScreen(
    onExit: () -> Unit
) {
    BackHandler(onBack = onExit)
    val viewModel: ThemeViewModel = getViewModel()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val color by viewModel.color.collectAsStateWithLifecycle()
    ThemeScreen(
        onBack = onExit,
        theme = theme,
        onThemeChange = viewModel::updateTheme,
        color = color,
        onColorChange = viewModel::updateColor
    )
}

@Composable
fun ThemeScreen(
    onBack: () -> Unit,
    theme: ThemeSetting,
    onThemeChange: (ThemeSetting) -> Unit,
    color: ColorSetting,
    onColorChange: (ColorSetting) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
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
                    ThemeSetting.entries.forEachIndexed { i, it ->
                        SegmentedButton(
                            selected = theme == it,
                            onClick = { onThemeChange(it) },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = ThemeSetting.entries.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(
                                    active = theme == it,
                                    inactiveContent = {
                                        val drawableRes = when (it) {
                                            ThemeSetting.System -> R.drawable.ic_contrast
                                            ThemeSetting.Dark -> R.drawable.ic_moon
                                            ThemeSetting.Light -> R.drawable.ic_sun
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
                            val textRes = remember(it) {
                                when (it) {
                                    ThemeSetting.System -> R.string.theme_theme_system
                                    ThemeSetting.Dark -> R.string.theme_theme_dark
                                    ThemeSetting.Light -> R.string.theme_theme_light
                                }
                            }
                            Text(stringResource(textRes))
                        }
                    }
                }
            }
            items(ColorSetting.validEntries) { colorSetting ->
                MauthTheme(
                    theme = theme,
                    color = colorSetting
                ) {
                    ThemeColorCard(
                        onClick = {
                            onColorChange(colorSetting)
                        },
                        name = {
                            val nameRes = remember(colorSetting) {
                                when (colorSetting) {
                                    ColorSetting.Dynamic -> R.string.theme_colors_dynamic
                                    ColorSetting.MothPurple -> R.string.theme_colors_purple
                                    ColorSetting.BlueberryBlue -> R.string.theme_colors_blue
                                    ColorSetting.PickleYellow -> R.string.theme_colors_yellow
                                    ColorSetting.ToxicGreen -> R.string.theme_colors_green
                                    ColorSetting.LeatherOrange -> R.string.theme_colors_orange
                                    ColorSetting.OceanTurquoise -> R.string.theme_colors_turquoise
                                }
                            }
                            Text(stringResource(nameRes))
                        },
                        selected = color == colorSetting
                    )
                }
            }
        }
    }
}