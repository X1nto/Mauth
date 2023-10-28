package com.xinto.mauth.ui.screen.about.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.theme.MauthTheme

@Composable
fun LinkedButtonsRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clip(MaterialTheme.shapes.extraLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
}

@Composable
fun RowScope.LinkedButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.weight(1f),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                propagateMinConstraints = true
            ) {
                icon()
            }
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                title()
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun LinkedButtonsPreview() {
    MauthTheme {
        val data = remember {
            listOf(
                R.drawable.ic_info to "Info",
                R.drawable.ic_edit to "Edit",
                R.drawable.ic_settings to "Settings",
            )
        }
        LinkedButtonsRow {
            data.forEach {
                LinkedButton(
                    onClick = { /*TODO*/ },
                    icon = {
                        Icon(
                            painter = painterResource(it.first),
                            contentDescription = null
                        )
                    },
                    title = {
                        Text(it.second)
                    }
                )
            }
        }
    }
}