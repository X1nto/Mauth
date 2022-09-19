package com.xinto.mauth.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun AddAccountScreen(
    navigator: MauthNavigator,
    viewModel: AddAccountViewModel = getViewModel()
) {
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
                    IconButton(onClick = { navigator.pop() }) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
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
            item {
                Spacer(Modifier.height(8.dp))
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = viewModel.label,
                        onValueChange = viewModel::updateLabel,
                        label = {
                            Text("Label")
                        },
                        placeholder = {
                            Text("John Doe")
                        },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = viewModel.issuer,
                        onValueChange = viewModel::updateIssuer,
                        label = {
                            Text("Issuer")
                        },
                        placeholder = {
                            Text("Your mom")
                        },
                        singleLine = true
                    )
                }
            }
            item {
                var shown by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    modifier = Modifier.fillParentMaxWidth(),
                    value = viewModel.secret,
                    onValueChange = viewModel::updateSecret,
                    label = {
                        Text("Secret")
                    },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { shown = !shown }) {
                            Icon(
                                imageVector = if (shown) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (shown) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        }
    }

}