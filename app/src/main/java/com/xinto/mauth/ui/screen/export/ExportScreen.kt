package com.xinto.mauth.ui.screen.export

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainExportAccount
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.screen.export.component.ZxingQrImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun ExportScreen(
    onBackNavigate: () -> Unit,
    accounts: List<UUID>
) {
    BackHandler(onBack = onBackNavigate)
    val viewModel: ExportViewModel = koinViewModel {
        parametersOf(accounts)
    }

    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val individualState by viewModel.individualState.collectAsStateWithLifecycle()
    val batchState by viewModel.batchState.collectAsStateWithLifecycle()

    ExportScreen(
        onBackNavigate = onBackNavigate,
        onCopyUrlToClipboard = {
            viewModel.copyUrlToClipboard(
                label = it.label,
                url = it.url
            )
        },
        mode = mode,
        onModeSelect = viewModel::switchMode,
        individualState = individualState,
        batchState = batchState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBackNavigate: () -> Unit,
    onCopyUrlToClipboard: (DomainExportAccount) -> Unit,
    individualState: ExportScreenState,
    batchState: BatchExportState,
    mode: ExportMode,
    onModeSelect: (ExportMode) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackNavigate) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                ExportMode.entries.forEachIndexed { i, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { onModeSelect(m) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = ExportMode.entries.size)
                    ) {
                        Text(m.name)
                    }
                }
            }

            when (mode) {
                ExportMode.Batch -> {
                    BatchExports(
                        modifier = Modifier.weight(1f),
                        state = batchState
                    )
                }
                ExportMode.Individual -> {
                    IndividualExports(
                        modifier = Modifier.weight(1f),
                        state = individualState,
                        onCopyUrlToClipboard = onCopyUrlToClipboard
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchExports(
    modifier: Modifier = Modifier,
    state: BatchExportState
) {
    when (state) {
        is BatchExportState.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is BatchExportState.Success -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pagerState = rememberPagerState { state.data.size }
                HorizontalPager(pagerState) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 1.dp
                    ) {
                        ZxingQrImage(
                            data = state.data[it],
                            size = 512,
                            backgroundColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (state.data.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(state.data.size) {
                            val selectedBackgroundColor = MaterialTheme.colorScheme.primary
                            val unselectedBackgroundColor = MaterialTheme.colorScheme.secondary
                            Box(
                                modifier = Modifier
                                    .drawBehind {
                                        val color =
                                            if (pagerState.currentPage == it) selectedBackgroundColor else unselectedBackgroundColor
                                        drawCircle(color)
                                    }
                                    .animateContentSize()
                                    .size(if (pagerState.currentPage == it) 16.dp else 12.dp)
                            )
                        }
                    }
                }
            }
        }
        is BatchExportState.Error -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun IndividualExports(
    modifier: Modifier = Modifier,
    state: ExportScreenState,
    onCopyUrlToClipboard: (DomainExportAccount) -> Unit
) {
    when (state) {
        is ExportScreenState.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ExportScreenState.Success -> {
            LazyVerticalStaggeredGrid(
                modifier = modifier,
                columns = StaggeredGridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(16.dp),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = state.accounts,
                    key = { it.id }
                ) { account ->
                    Surface(
                        onClick = { onCopyUrlToClipboard(account) },
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                ZxingQrImage(
                                    modifier = Modifier.fillMaxSize(),
                                    data = account.url,
                                    backgroundColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (account.icon != null) {
                                            UriImage(uri = account.icon)
                                        } else {
                                            Text(
                                                text = account.shortLabel,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }
                                }
                                Column {
                                    if (account.issuer.isNotEmpty()) {
                                        Text(
                                            text = account.issuer,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Text(
                                        text = account.label,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        is ExportScreenState.Error -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null
                )
            }
        }
    }
}