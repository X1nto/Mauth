package com.xinto.mauth.ui.screen.theme.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.ui.theme.MauthTheme

@Composable
fun ThemeColorCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    selected: Boolean,
    name: @Composable () -> Unit,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                ElevatedCard {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .height(16.dp)
                                .weight(1f)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColorFor(containerColor),
                    LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Medium)
                ) {
                    AnimatedVisibility(visible = selected) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null
                        )
                    }
                    Box(modifier = Modifier.padding(start = 0.dp)) {
                        name()
                    }
                }
            }
        }
    }
}

@Composable
@PreviewDynamicColors
fun ThemeColorCard_Preview_Unselected() {
    MauthTheme(color = ColorSetting.Dynamic) {
        ThemeColorCard(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight(),
            onClick = {},
            name = {
                Text("Color scheme")
            },
            selected = false
        )
    }
}

@Composable
@PreviewDynamicColors
fun ThemeColorCard_Preview_Selected() {
    MauthTheme(color = ColorSetting.Dynamic) {
        ThemeColorCard(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight(),
            onClick = {},
            name = {
                Text("Color scheme")
            },
            selected = true
        )
    }
}