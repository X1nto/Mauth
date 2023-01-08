package com.xinto.mauth.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.contracts.PickVisualMediaPersistent
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.component.singleItem
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import com.xinto.mauth.ui.viewmodel.AddEditAccountViewModel
import com.xinto.mauth.ui.viewmodel.EditAccountViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

sealed interface AddEditAccountState {
    object Loading : AddEditAccountState
    object Success : AddEditAccountState
    object Error : AddEditAccountState
}

@Composable
fun AddAccountScreen(
    navigator: MauthNavigator,
    accountInfo: DomainAccountInfo
) {
    val viewModel: AddAccountViewModel = koinViewModel { parametersOf(accountInfo) }
    AddEditAccountScreenImpl(
        navigator = navigator,
        viewModel = viewModel,
        title = stringResource(R.string.addeditaccount_title_add)
    )
}

@Composable
fun EditAccountScreen(
    navigator: MauthNavigator,
    accountId: UUID
) {
    val viewModel: EditAccountViewModel = koinViewModel { parametersOf(accountId) }
    AddEditAccountScreenImpl(
        navigator = navigator,
        viewModel = viewModel,
        title = stringResource(R.string.addeditaccount_title_edit)
    )
}

@Composable
private fun AddEditAccountScreenImpl(
    navigator: MauthNavigator,
    viewModel: AddEditAccountViewModel,
    title: String,
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }
    Scaffold(
        topBar = {
            AddEditAccountTopbar(
                title = title,
                onSave = {
                    if (viewModel.save()) {
                        navigator.pop()
                    }
                },
                onClose = {
                    showExitDialog = true
                }
            )
        },
    ) { paddingValues ->
        when (viewModel.state) {
            is AddEditAccountState.Loading -> {

            }
            is AddEditAccountState.Success -> {
                AddEditAccountSuccess(paddingValues, viewModel)
            }
            is AddEditAccountState.Error -> {

            }
        }
    }
    if (showExitDialog) {
        ExitDialog(
            onConfirm = {
                showExitDialog = false
                navigator.pop()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
}

@Composable
private fun ExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.addeditaccount_discard_title))
        },
        text = {
            Text(stringResource(R.string.addeditaccount_discard_subtitle))
        },
        confirmButton = {
            FilledTonalButton(onConfirm) {
                Text(stringResource(R.string.addeditaccount_discard_buttons_discard))
            }
        },
        dismissButton = {
            TextButton(onDismiss) {
                Text(stringResource(R.string.addeditaccount_discard_buttons_cancel))
            }
        }
    )
}

@Composable
private fun AddEditAccountSuccess(
    paddingValues: PaddingValues,
    viewModel: AddEditAccountViewModel
) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2)
    ) {
        singleItem {
            AccountIcon(
                uri = viewModel.imageUri,
                onUriChange = viewModel::updateImageUri
            )
        }
        singleItem {
            Spacer(Modifier.height(8.dp))
        }
        singleItem {
            AccountLabel(
                label = viewModel.label,
                onLabelChange = viewModel::updateLabel,
                isError = viewModel.errorLabel
            )
        }
        singleItem {
            AccountIssuer(
                issuer = viewModel.issuer,
                onIssuerChange = viewModel::updateIssuer
            )
        }
        singleItem {
            AccountSecret(
                secret = viewModel.secret,
                onSecretChange = viewModel::updateSecret
            )
        }
        item {
            AccountType(
                type = viewModel.type,
                onTypeChange = viewModel::updateType
            )
        }
        item {
            AccountAlgorithm(
                algorithm = viewModel.algorithm,
                onAlgorithmChange = viewModel::updateAlgorithm
            )
        }
        item {
            AccountDigits(
                digits = viewModel.digits,
                onDigitsChange = viewModel::updateDigits,
                isError = viewModel.errorDigits
            )
        }
        item {
            AccountPeriodOrCounter(
                type = viewModel.type,
                period = viewModel.period,
                onPeriodChange = viewModel::updatePeriod,
                periodIsError = viewModel.errorPeriod,
                counter = viewModel.counter,
                onCounterChange = viewModel::updateCounter,
                counterIsError = viewModel.errorPeriod
            )
        }
        if (viewModel.id != null) {
            singleItem {
                Text(
                    text = viewModel.id.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AddEditAccountTopbar(
    title: String,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null
                )
            }
        },
        actions = {
            TextButton(onSave) {
                Text(stringResource(R.string.addeditaccount_actions_save))
            }
        }
    )
}

@Composable
private fun AccountLabel(
    label: String,
    onLabelChange: (String) -> Unit,
    isError: Boolean,
) {
    OutlinedTextField(
        value = label,
        onValueChange = onLabelChange,
        label = {
            Text(stringResource(R.string.addeditaccount_data_label))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Label,
                contentDescription = null
            )
        },
        singleLine = true,
        isError = isError
    )
}

@Composable
private fun AccountIssuer(
    issuer: String,
    onIssuerChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier,
        value = issuer,
        onValueChange = onIssuerChange,
        label = {
            Text(stringResource(R.string.addeditaccount_data_issuer))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Apartment,
                contentDescription = null
            )
        },
        singleLine = true
    )
}

@Composable
private fun AccountDigits(
    digits: String,
    onDigitsChange: (String) -> Unit,
    isError: Boolean,
) {
    val keyboardOptions = remember {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    }
    OutlinedTextField(
        value = digits,
        onValueChange = onDigitsChange,
        label = {
            Text(stringResource(R.string.addeditaccount_data_digits))
        },
        keyboardOptions = keyboardOptions,
        isError = isError
    )
}

@Composable
private fun AccountPeriodOrCounter(
    type: OtpType,
    counter: String,
    onCounterChange: (String) -> Unit,
    counterIsError: Boolean,
    period: String,
    onPeriodChange: (String) -> Unit,
    periodIsError: Boolean
) {
    val keyboardOptions = remember {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    }
    AnimatedContent(
        targetState = type,
        transitionSpec = {
            slideIntoContainer(AnimatedContentScope.SlideDirection.Up) + fadeIn() with
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
        }
    ) { animatedType ->
        when (animatedType) {
            OtpType.Hotp -> {
                OutlinedTextField(
                    value = counter,
                    onValueChange = onCounterChange,
                    label = {
                        Text(stringResource(R.string.addeditaccount_data_counter))
                    },
                    keyboardOptions = keyboardOptions,
                    isError = counterIsError
                )
            }
            OtpType.Totp -> {
                OutlinedTextField(
                    value = period,
                    onValueChange = onPeriodChange,
                    label = {
                        Text(stringResource(R.string.addeditaccount_data_period))
                    },
                    keyboardOptions = keyboardOptions,
                    isError = periodIsError
                )
            }
        }
    }
}

@Composable
private fun AccountAlgorithm(
    algorithm: OtpDigest,
    onAlgorithmChange: (OtpDigest) -> Unit,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            value = algorithm.name,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(stringResource(R.string.addeditaccount_data_algorithm))
            },
            trailingIcon = {
                val iconRotation by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    modifier = Modifier.rotate(iconRotation),
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            OtpDigest.values().forEach {
                DropdownMenuItem(
                    text = {
                        Text(it.name)
                    },
                    onClick = {
                        onAlgorithmChange(it)
                        setExpanded(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun AccountType(
    type: OtpType,
    onTypeChange: (OtpType) -> Unit,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            value = type.name,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(stringResource(R.string.addeditaccount_data_type))
            },
            trailingIcon = {
                val iconRotation by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    modifier = Modifier.rotate(iconRotation),
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            OtpType.values().forEach {
                DropdownMenuItem(
                    text = {
                        Text(it.name)
                    },
                    onClick = {
                        onTypeChange(it)
                        setExpanded(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun AccountSecret(
    secret: String,
    onSecretChange: (String) -> Unit,
) {
    var shown by rememberSaveable { mutableStateOf(false) }
    val keyboardOptions =
        remember { KeyboardOptions(keyboardType = KeyboardType.Password) }
    val passwordTransformation = remember { PasswordVisualTransformation() }
    OutlinedTextField(
        modifier = Modifier,
        value = secret,
        onValueChange = onSecretChange,
        label = {
            Text(stringResource(R.string.addeditaccount_data_secret))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Key,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { shown = !shown }) {
                Icon(
                    imageVector = if (shown) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        singleLine = true,
        visualTransformation = if (shown) VisualTransformation.None else passwordTransformation,
        keyboardOptions = keyboardOptions
    )
}

@Composable
private fun AccountIcon(
    uri: Uri?,
    onUriChange: (Uri?) -> Unit,
) {
    val imageSelectLauncher = rememberLauncherForActivityResult(PickVisualMediaPersistent()) {
        onUriChange(it)
    }
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
            border = BorderStroke(
                width = 1.dp,
                SolidColor(MaterialTheme.colorScheme.outline)
            ),
            onClick = {
                imageSelectLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        ) {
            if (uri != null) {
                UriImage(uri = uri)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Rounded.AddAPhoto,
                        contentDescription = null
                    )
                }
            }
        }
    }
}