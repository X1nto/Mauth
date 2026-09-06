package com.xinto.mauth.ui.screen.export

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.model.BackupFormat
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun FileExportDialog(
    modifier: Modifier = Modifier,
    accounts: List<UUID>,
    format: BackupFormat,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FileExportViewModel = koinViewModel {
        parametersOf(accounts)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    var pendingPassword by rememberSaveable { mutableStateOf<CharSequence?>(null) }

    val destinationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(format.mimeType)) { uri ->
        if (uri != null) {
            viewModel.export(
                format = format,
                destination = uri,
                password = pendingPassword
            )
            pendingPassword = null
        } else {
            onDismissRequest()
        }
    }

    LaunchedEffect(state) {
        if (state == FileExportState.Succeeded) {
            onDismissRequest()
            Toast.makeText(context, R.string.export_file_status_success, Toast.LENGTH_SHORT).show()
        } else if (state == FileExportState.Failed) {
            Toast.makeText(context, R.string.export_file_status_failure, Toast.LENGTH_SHORT).show()
            onDismissRequest()
        }
    }

    if (format.supportsEncryption) {
        FileExportDialog(
            modifier = modifier,
            state = state,
            format = format,
            onDismissRequest = onDismissRequest,
            onExport = {
                pendingPassword = it
                destinationLauncher.launch("mauth-export.${format.extension}")
            }
        )
    } else {
        LaunchedEffect(Unit) {
            destinationLauncher.launch("mauth-export.${format.extension}")
        }
    }

}

@Composable
private fun FileExportDialog(
    modifier: Modifier = Modifier,
    state: FileExportState,
    format: BackupFormat,
    onExport: (CharSequence?) -> Unit,
    onDismissRequest: () -> Unit
) {
    val passwordState = rememberTextFieldState()
    val (showPassword, setShowPassword) = retain { mutableStateOf(false) }
    val (encrypt, setEncrypt) = retain { mutableStateOf(true) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            val titleRes = when (format) {
                BackupFormat.MauthJson -> R.string.export_file_title_mauth
                BackupFormat.AegisVault -> R.string.export_file_title_aegis
                BackupFormat.FreeOtpPlusJson -> R.string.export_file_title_freeotpplus
                BackupFormat.UriList -> R.string.export_file_title_otpauth
            }
            Text(stringResource(titleRes))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedSecureTextField(
                    state = passwordState,
                    label = { Text(stringResource(R.string.export_file_input_password)) },
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = if (showPassword) KeyboardType.PasswordVisible else KeyboardType.Password,
                    ),
                    textObfuscationMode = if (showPassword) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                    trailingIcon = {
                        val descriptionRes = if (showPassword) R.string.export_file_action_password_hide else R.string.export_file_action_password_show
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = { PlainTooltip { Text(stringResource(descriptionRes)) } },
                            state = rememberTooltipState(),
                        ) {
                            IconToggleButton(
                                checked = showPassword,
                                onCheckedChange = setShowPassword
                            ) {
                                val iconRes = if (showPassword) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = stringResource(descriptionRes)
                                )
                            }
                        }
                    },
                    enabled = encrypt
                )
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .toggleable(
                            value = encrypt,
                            onValueChange = setEncrypt
                        )
                        .padding(top = 4.dp, bottom = 4.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(stringResource(R.string.export_file_action_encrypt))
                    Spacer(Modifier.weight(1f))
                    Checkbox(
                        checked = encrypt,
                        onCheckedChange = null
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.export_file_action_cancel))
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    val password = passwordState.text.takeIf { encrypt && it.isNotBlank() }
                    onExport(password)
                },
                enabled = when (state) {
                    FileExportState.Idle -> !encrypt || passwordState.text.isNotBlank()
                    FileExportState.Working -> false
                    else -> true
                }
            ) {
                if (state == FileExportState.Working) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.export_file_action_export))
                }
            }
        }
    )
}

