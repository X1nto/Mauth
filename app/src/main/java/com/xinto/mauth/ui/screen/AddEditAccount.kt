package com.xinto.mauth.ui.screen

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
    )
}

@Composable
private fun AddEditAccountScreenImpl(
    navigator: MauthNavigator,
    viewModel: AddEditAccountViewModel,
) {
    var showExitDialog by remember { mutableStateOf(false) }
    val imageSelectLauncher = rememberLauncherForActivityResult(PickVisualMediaPersistent()) {
        viewModel.updateImageUri(it)
    }
    BackHandler {
        navigator.pop()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (viewModel.id != null) {
                        Text(stringResource(R.string.addeditaccount_title_edit))
                    } else {
                        Text(stringResource(R.string.addeditaccount_title_add))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (viewModel.save()) {
                            navigator.pop()
                        }
                    }) {
                        Text(stringResource(R.string.addeditaccount_actions_save))
                    }
                }
            )
        },
    ) { paddingValues ->
        when (viewModel.state) {
            is AddEditAccountState.Success -> {
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
                                if (viewModel.imageUri != null) {
                                    UriImage(uri = viewModel.imageUri!!)
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
                    singleItem {
                        Spacer(Modifier.height(8.dp))
                    }
                    singleItem {
                        OutlinedTextField(
                            value = viewModel.label,
                            onValueChange = viewModel::updateLabel,
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
                            isError = viewModel.errorLabel
                        )
                    }
                    singleItem {
                        OutlinedTextField(
                            modifier = Modifier,
                            value = viewModel.issuer,
                            onValueChange = viewModel::updateIssuer,
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
                    singleItem {
                        var shown by rememberSaveable { mutableStateOf(false) }
                        val keyboardOptions =
                            remember { KeyboardOptions(keyboardType = KeyboardType.Password) }
                        val passwordTransformation = remember { PasswordVisualTransformation() }
                        OutlinedTextField(
                            modifier = Modifier,
                            value = viewModel.secret,
                            onValueChange = viewModel::updateSecret,
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
                    item {
                        val (expanded, setExpanded) = remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = setExpanded
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(),
                                value = viewModel.type.name,
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
                                            viewModel.updateType(it)
                                            setExpanded(false)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val (expanded, setExpanded) = remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = setExpanded
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(),
                                value = viewModel.algorithm.name,
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
                                            viewModel.updateAlgorithm(it)
                                            setExpanded(false)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val keyboardOptions = remember {
                            KeyboardOptions(keyboardType = KeyboardType.Number)
                        }
                        OutlinedTextField(
                            value = viewModel.digits,
                            onValueChange = viewModel::updateDigits,
                            label = {
                                Text(stringResource(R.string.addeditaccount_data_digits))
                            },
                            keyboardOptions = keyboardOptions,
                            isError = viewModel.errorDigits
                        )
                    }
                    item {
                        val keyboardOptions = remember {
                            KeyboardOptions(keyboardType = KeyboardType.Number)
                        }
                        AnimatedContent(
                            targetState = viewModel.type,
                            transitionSpec = {
                                slideIntoContainer(AnimatedContentScope.SlideDirection.Up) + fadeIn() with
                                    slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
                            }
                        ) { animatedType ->
                            when (animatedType) {
                                OtpType.Hotp -> {
                                    OutlinedTextField(
                                        value = viewModel.counter,
                                        onValueChange = viewModel::updateCounter,
                                        label = {
                                            Text(stringResource(R.string.addeditaccount_data_counter))
                                        },
                                        keyboardOptions = keyboardOptions,
                                        isError = viewModel.errorCounter
                                    )
                                }
                                OtpType.Totp -> {
                                    OutlinedTextField(
                                        value = viewModel.period,
                                        onValueChange = viewModel::updatePeriod,
                                        label = {
                                            Text(stringResource(R.string.addeditaccount_data_period))
                                        },
                                        keyboardOptions = keyboardOptions,
                                        isError = viewModel.errorPeriod
                                    )
                                }
                            }
                        }
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
            else -> {

            }
        }
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },
            title = {
                Text(stringResource(R.string.addeditaccount_discard_title))
            },
            text = {
                Text(stringResource(R.string.addeditaccount_discard_subtitle))
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navigator.pop()
                }) {
                    Text(stringResource(R.string.addeditaccount_discard_buttons_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.addeditaccount_discard_buttons_cancel))
                }
            }
        )
    }
}