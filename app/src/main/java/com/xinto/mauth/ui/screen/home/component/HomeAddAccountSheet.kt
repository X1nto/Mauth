package com.xinto.mauth.ui.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import kotlinx.coroutines.launch

@Composable
fun HomeAddAccountSheet(
    onDismiss: () -> Unit,
    onManualEnterClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onChooseImage: () -> Unit,
) {
    MaterialBottomSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_addaccount_title)) },
        subtitle = { Text(stringResource(R.string.home_addaccount_subtitle)) },
    ) {
        Column(
            modifier = Modifier.clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            FullWidthButton(
                onClick = onScanQrClick,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_qr_code_scanner),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.home_addaccount_data_scanqr)) }
            )
            FullWidthButton(
                onClick = onChooseImage,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_qr_code_2),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.home_addaccount_data_imageqr)) }
            )
            FullWidthButton(
                onClick = onManualEnterClick,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_password),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.home_addaccount_data_manual)) }
            )
        }
    }
}

@Composable
private fun ColumnScope.FullWidthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                text()
            }
        }
    }
}

@Composable
private fun MaterialBottomSheetDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutine = rememberCoroutineScope()
    DisposableEffect(Unit) {
        coroutine.launch {
            state.show()
        }
        onDispose {
            coroutine.launch {
                state.hide()
            }
        }
    }
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                )
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.headlineMedium) {
                title()
            }
            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                subtitle()
            }
            Box(modifier = Modifier.padding(top = 12.dp)) {
                body()
            }
        }
    }
}