package com.xinto.mauth.ui.screen.export

import android.content.ClipData
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.AlertDialog
import com.xinto.mauth.ui.screen.export.component.ExportQrCode
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun AccountExportDialog(
    modifier: Modifier = Modifier,
    account: UUID,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val viewModel: AccountExportViewModel = koinViewModel {
        parametersOf(account)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    AccountExportDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        state = state,
        onUriClick = { label, data ->
            val clipData = ClipData.newPlainText(label, data).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    description.extras = PersistableBundle().apply {
                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                    }
                }
            }
            coroutineScope.launch {
                clipboard.setClipEntry(ClipEntry(clipData))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(context, R.string.export_url_copy_success, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountExportDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    state: AccountExportState,
    onUriClick: (label: String, data: String) -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        when (state) {
            is AccountExportState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is AccountExportState.Success -> {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportQrCode(
                        modifier = Modifier.fillMaxWidth(),
                        data = state.url
                    )

                    Surface(
                        onClick = { onUriClick(state.label, state.url) },
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.export_account_copy),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_copy_all),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            is AccountExportState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        contentDescription = null
                    )
                }
            }
        }
    }
}