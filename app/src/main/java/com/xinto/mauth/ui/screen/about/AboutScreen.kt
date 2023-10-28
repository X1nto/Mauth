package com.xinto.mauth.ui.screen.about

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xinto.mauth.BuildConfig
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.rememberUriHandler
import com.xinto.mauth.ui.screen.about.component.LinkedButton
import com.xinto.mauth.ui.screen.about.component.LinkedButtonsRow
import com.xinto.mauth.ui.theme.MauthTheme

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val uriHandler = rememberUriHandler()
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(
                    modifier = Modifier.padding(0.dp),
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
            LinkedButtonsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                AboutLink.defaultLinks.forEach {
                    LinkedButton(
                        onClick = { uriHandler.openUrl(it.url) },
                        icon = {
                            Icon(
                                painter = painterResource(it.icon),
                                contentDescription = null
                            )
                        },
                        title = { Text(stringResource(it.title)) }
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun AboutScreenPreview() {
    MauthTheme {
        AboutScreen(onBack = {})
    }
}