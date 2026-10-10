package com.xinto.mauth.ui.screen.importer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.importer.ImportSource
import com.xinto.mauth.core.backup.model.toFilterArray
import com.xinto.mauth.domain.backup.ImportOutcome
import com.xinto.mauth.ui.component.adaptivePainterResource
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.preview.Screenshot
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ImportScreen(
    onBack: () -> Unit,
    onParsed: (ImportOutcome.Parsed) -> Unit,
    onScanQrCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ImportViewModel = koinViewModel()
    val attempt by viewModel.attempt.collectAsStateWithLifecycle()

    var chosen by rememberSaveable { mutableStateOf<ImportSource?>(null) }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val source = chosen
        if (uri != null && source != null) {
            viewModel.read(uri, source)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.parsed.collect(onParsed)
    }

    ImportScreen(
        modifier = modifier,
        sources = ImportSource.appSources,
        otherSources = ImportSource.otherSources,
        onBack = onBack,
        onScanQrCode = onScanQrCode,
        onChooseFile = { source ->
            chosen = source
            openLauncher.launch(source.mimeTypes.toFilterArray())
        }
    )

    if (attempt != null) {
        ImportAttemptDialog(
            attempt = attempt!!,
            onPassword = viewModel::submitPassword,
            onRetryAs = viewModel::retryAs,
            onDismiss = viewModel::dismiss
        )
    }
}

@Composable
fun ImportScreen(
    sources: List<ImportSource>,
    otherSources: List<ImportSource>,
    onBack: () -> Unit,
    onChooseFile: (ImportSource) -> Unit,
    onScanQrCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .wrapContentWidth()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SourceGroup(header = stringResource(R.string.import_category_apps)) {
                sources.forEachIndexed { index, source ->
                    val detail = source.detail()
                    ListItem(
                        onClick = { onChooseFile(source) },
                        leadingContent = { SourceAvatar(icon = source.icon) },
                        supportingContent = if (detail == null) null else { -> Text(text = detail) },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = sources.size
                        ),
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(source.titleRes))
                    }
                }
            }

            SourceGroup(header = stringResource(R.string.import_category_other)) {
                val count = otherSources.size + 1
                ListItem(
                    onClick = onScanQrCode,
                    leadingContent = { SourceAvatar(icon = SourceIcon.Glyph(R.drawable.ic_qr_code_2)) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = count
                    ),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.import_source_googleauth))
                }
                otherSources.forEachIndexed { index, source ->
                    val detail = source.detail()
                    ListItem(
                        onClick = { onChooseFile(source) },
                        leadingContent = { SourceAvatar(icon = source.icon) },
                        supportingContent = if (detail == null) null else { -> Text(text = detail) },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index + 1,
                            count = count
                        ),
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(source.titleRes))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportSource.detail(): String? {
    val encryption = encryptionRes?.let { stringResource(it) }
    return listOfNotNull(format, encryption)
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" · ")
}

@Composable
private fun SourceGroup(
    header: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = header,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )
    }
}

@Composable
private fun SourceAvatar(
    icon: SourceIcon,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .then(if (icon is SourceIcon.AppIcon) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        when (icon) {
            is SourceIcon.AppIcon -> {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = adaptivePainterResource(icon.res),
                    contentDescription = null
                )
            }

            is SourceIcon.Glyph -> {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(icon.res),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportScreen_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ImportScreen(
                modifier = Modifier.fillMaxSize(),
                sources = ImportSource.appSources,
                otherSources = ImportSource.otherSources,
                onBack = {},
                onChooseFile = {},
                onScanQrCode = {}
            )
        }
    }
}
