package com.xinto.mauth.ui.screen.export

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.screen.export.state.ExportScreenError
import com.xinto.mauth.ui.screen.export.state.ExportScreenLoading
import com.xinto.mauth.ui.screen.export.state.ExportScreenSuccess
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExportScreen(
        onBackNavigate = onBackNavigate,
        state = state
    )
}

@Composable
fun ExportScreen(
    onBackNavigate: () -> Unit,
    state: ExportScreenState
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(text = stringResource(R.string.export_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackNavigate) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {

                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is ExportScreenState.Loading -> {
                    ExportScreenLoading()
                }
                is ExportScreenState.Success -> {
                    ExportScreenSuccess(accounts = state.accounts)
                }
                is ExportScreenState.Error -> {
                    ExportScreenError()
                }
            }
        }
    }
}
