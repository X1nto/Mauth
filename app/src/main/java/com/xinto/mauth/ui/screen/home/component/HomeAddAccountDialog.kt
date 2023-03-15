package com.xinto.mauth.ui.screen.home.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.holix.android.bottomsheetdialog.compose.BottomSheetDialog
import com.holix.android.bottomsheetdialog.compose.BottomSheetDialogProperties
import com.xinto.mauth.R

@Composable
fun HomeAddAccountDialog(
    onDismiss: () -> Unit,
    onManualEnterClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onChooseImage: () -> Unit,
) {
    MaterialBottomSheetDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.home_addaccount_title))
        },
        subtitle = {
            Text(stringResource(R.string.home_addaccount_subtitle))
        },
    ) {
        Column(
            modifier = Modifier.clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            FullWidthButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onScanQrClick,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.home_addaccount_data_scanqr))
                },
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            FullWidthButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onChooseImage,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.home_addaccount_data_imageqr))
                },
                color = MaterialTheme.colorScheme.tertiaryContainer,
            )
            FullWidthButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onManualEnterClick,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Password,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.home_addaccount_data_manual))
                },
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

@Composable
private fun FullWidthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = color,
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
    properties: BottomSheetDialogProperties = BottomSheetDialogProperties(),
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    BottomSheetDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
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
}