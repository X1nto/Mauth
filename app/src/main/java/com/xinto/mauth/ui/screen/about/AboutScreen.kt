package com.xinto.mauth.ui.screen.about

import android.content.ClipData
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.xinto.mauth.BuildConfig
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.mediumClickableItem
import com.xinto.mauth.ui.component.rememberUriHandler
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.util.installerPackageName

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val uriHandler = rememberUriHandler()
    val buildInfo = remember {
        buildString {
            appendLine("Mauth ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            append("${Build.MANUFACTURER} ${Build.MODEL}")
        }
    }
    val installSource = remember {
        val installerPackageName = context.packageManager.installerPackageName
        when (installerPackageName) {
            "com.android.vending" -> InstallSource.GooglePlay
            "org.fdroid.fdroid", "org.fdroid.fdroid.privileged" -> InstallSource.FDroid
            else -> InstallSource.Manual
        }
    }
    AboutScreen(
        modifier = modifier,
        onBack = onBack,
        versionName = BuildConfig.VERSION_NAME,
        installSource = installSource,
        onSourceClick = { uriHandler.openUrl("https://github.com/X1nto/Mauth") },
        onFeedbackClick = { uriHandler.openUrl("https://github.com/X1nto/Mauth/issues") },
        onPrivacyClick = { uriHandler.openUrl("https://raw.githubusercontent.com/X1nto/Mauth/refs/heads/master/POLICY") },
        onLicenseClick = { uriHandler.openUrl("https://github.com/X1nto/Mauth/blob/master/LICENSE") },
        onCopyBuildInfo = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Mauth build info", buildInfo)))
                Toast.makeText(context, R.string.about_info_version_copied, Toast.LENGTH_SHORT).show()
            }
        },
    )
}

enum class InstallSource(@param:StringRes val labelRes: Int) {
    FDroid(R.string.about_info_source_fdroid),
    GooglePlay(R.string.about_info_source_gplay),
    Manual(R.string.about_info_source_other)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    versionName: String,
    installSource: InstallSource,
    onSourceClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onCopyBuildInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
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
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .widthIn(max = 600.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialShapes.Clover4Leaf.toShape(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                    )
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            val sourceLabel = stringResource(R.string.about_links_source)
            val feedbackLabel = stringResource(R.string.about_links_feedback)
            ButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) }
            ) {
                mediumClickableItem(
                    onClick = onSourceClick,
                    label = sourceLabel,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder_code),
                            contentDescription = null
                        )
                    }
                )
                mediumClickableItem(
                    onClick = onFeedbackClick,
                    label = feedbackLabel,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_bug),
                            contentDescription = null
                        )
                    }
                )
            }

            val listItemColors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCopyBuildInfo,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_deployed_code),
                            contentDescription = null
                        )
                    },
                    supportingContent = { Text(stringResource(R.string.about_info_version_description, versionName)) },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy_all),
                            contentDescription = null
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
                    colors = listItemColors,
                    verticalAlignment = Alignment.CenterVertically,
                    content = { Text(stringResource(R.string.about_info_version)) }
                )
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPrivacyClick,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_privacy_tip),
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                    colors = listItemColors,
                    verticalAlignment = Alignment.CenterVertically,
                    content = { Text(stringResource(R.string.about_info_privacy)) }
                )
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onLicenseClick,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_description),
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                    colors = listItemColors,
                    verticalAlignment = Alignment.CenterVertically,
                    content = { Text(stringResource(R.string.about_info_license)) }
                )
            }

            ListItem(
                modifier = Modifier.fillMaxWidth(),
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_apk_install),
                        contentDescription = null
                    )
                },
                supportingContent = { Text(stringResource(installSource.labelRes)) },
                shapes = ListItemDefaults.segmentedShapes(
                    index = 0,
                    count = 1,
                    defaultShapes = ListItemDefaults.shapes(MaterialTheme.shapes.large)
                ),
                colors = listItemColors,
                verticalAlignment = Alignment.CenterVertically,
                content = { Text(stringResource(R.string.about_info_source)) }
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AboutScreen_FDroid_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AboutScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                versionName = BuildConfig.VERSION_NAME,
                installSource = InstallSource.FDroid,
                onSourceClick = {},
                onFeedbackClick = {},
                onPrivacyClick = {},
                onLicenseClick = {},
                onCopyBuildInfo = {},
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AboutScreen_GooglePlay_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AboutScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                versionName = BuildConfig.VERSION_NAME,
                installSource = InstallSource.GooglePlay,
                onSourceClick = {},
                onFeedbackClick = {},
                onPrivacyClick = {},
                onLicenseClick = {},
                onCopyBuildInfo = {},
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AboutScreen_Manual_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AboutScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                versionName = BuildConfig.VERSION_NAME,
                installSource = InstallSource.Manual,
                onSourceClick = {},
                onFeedbackClick = {},
                onPrivacyClick = {},
                onLicenseClick = {},
                onCopyBuildInfo = {},
            )
        }
    }
}