package com.xinto.mauth.ui.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType
import com.xinto.mauth.ui.component.singleItem
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun AddAccountScreen(
    navigator: MauthNavigator,
    viewModel: AddAccountViewModel = getViewModel()
) {
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler {
        navigator.pop()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add an account")
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
                        viewModel.save()
                        navigator.pop()
                    }) {
                        Text("Save")
                    }
                }
            )
        },
    ) { paddingValues ->
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
                        )
                    ) {
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
            singleItem {
                Spacer(Modifier.height(8.dp))
            }
            singleItem {
                OutlinedTextField(
                    value = viewModel.label,
                    onValueChange = viewModel::updateLabel,
                    label = {
                        Text("Label")
                    },
                    placeholder = {
                        Text("John Doe")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Label,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                )
            }
            singleItem {
                OutlinedTextField(
                    modifier = Modifier,
                    value = viewModel.issuer,
                    onValueChange = viewModel::updateIssuer,
                    label = {
                        Text("Issuer")
                    },
                    placeholder = {
                        Text("Your mom")
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
                val keyboardOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Password) }
                val passwordTransformation = remember { PasswordVisualTransformation() }
                OutlinedTextField(
                    modifier = Modifier,
                    value = viewModel.secret,
                    onValueChange = viewModel::updateSecret,
                    label = {
                        Text("Secret")
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
                        value = viewModel.type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Type")
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
                        value = viewModel.algorithm.name,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Type")
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
                    value = viewModel.digits.toString(),
                    onValueChange = viewModel::updateDigits,
                    label = {
                        Text("Digits")
                    },
                    keyboardOptions = keyboardOptions
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
                                value = viewModel.counter.toString(),
                                onValueChange = viewModel::updateCounter,
                                label = {
                                    Text("Counter")
                                },
                                keyboardOptions = keyboardOptions
                            )
                        }
                        OtpType.Totp -> {
                            OutlinedTextField(
                                value = viewModel.period.toString(),
                                onValueChange = viewModel::updatePeriod,
                                label = {
                                    Text("Period")
                                },
                                keyboardOptions = keyboardOptions
                            )
                        }
                    }
                }
            }
        }
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },
            title = {
                Text("Discard changes?")
            },
            text = {
                Text("Your changes will not be saved.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navigator.pop()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}