package com.xinto.mauth.ui.screen.account.state

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.core.contracts.PickVisualMediaPersistent
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.ui.component.UriImage
import java.util.*

@Composable
fun AccountScreenSuccess(
    id: UUID?,
    icon: Uri?,
    onIconChange: (Uri?) -> Unit,
    label: String,
    onLabelChange: (String) -> Unit,
    issuer: String,
    onIssuerChange: (String) -> Unit,
    secret: String,
    onSecretChange: (String) -> Unit,
    type: OtpType,
    onTypeChange: (OtpType) -> Unit,
    digest: OtpDigest,
    onDigestChange: (OtpDigest) -> Unit,
    digits: Int,
    onDigitsChange: (Int) -> Unit,
    counter: Int,
    onCounterChange: (Int) -> Unit,
    period: Int,
    onPeriodChange: (Int) -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2)
    ) {
        singleItem {
            val imageSelectLauncher = rememberLauncherForActivityResult(
                PickVisualMediaPersistent()
            ) {
                onIconChange(it)
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    onClick = {
                        imageSelectLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    if (icon != null) {
                        UriImage(uri = icon)
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
            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                singleLine = true,
                label = {
                    Text(stringResource(R.string.addeditaccount_data_label))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Label,
                        contentDescription = null
                    )
                },
            )
        }
        singleItem {
            OutlinedTextField(
                value = issuer,
                onValueChange = onIssuerChange,
                singleLine = true,
                label = {
                    Text(stringResource(R.string.addeditaccount_data_issuer))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Apartment,
                        contentDescription = null
                    )
                },
            )
        }
        singleItem {
            var secretShown by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = secret,
                onValueChange = onSecretChange,
                singleLine = true,
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
                    IconButton(onClick = { secretShown = !secretShown }) {
                        Icon(
                            imageVector = if (secretShown) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = remember { PasswordVisualTransformation() },
                keyboardOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Password) }
            )
        }
        item {
            ComboBox(
                values = OtpType.values(),
                value = type,
                onValueChange = onTypeChange,
                label = {
                    Text(stringResource(R.string.addeditaccount_data_type))
                }
            )
        }
        item {
            ComboBox(
                values = OtpDigest.values(),
                value = digest,
                onValueChange = onDigestChange,
                label = {
                    Text(stringResource(R.string.addeditaccount_data_algorithm))
                }
            )
        }
        item {
            NumberField(
                value = digits,
                onValueChange = onDigitsChange,
                label = {
                    Text(stringResource(R.string.addeditaccount_data_digits))
                }
            )
        }
        item {
            SlideAnimatable(targetState = type) {
                when (it) {
                    OtpType.Totp -> {
                        NumberField(
                            value = period,
                            onValueChange = onPeriodChange,
                            label = {
                                Text(stringResource(R.string.addeditaccount_data_period))
                            }
                        )
                    }
                    OtpType.Hotp -> {
                        NumberField(
                            value = counter,
                            onValueChange = onCounterChange,
                            label = {
                                Text(stringResource(R.string.addeditaccount_data_counter))
                            }
                        )
                    }
                }
            }
        }
        if (id != null) {
            singleItem {
                Text(
                    text = id.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun <S> SlideAnimatable(
    targetState: S,
    content: @Composable (S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            slideIntoContainer(AnimatedContentScope.SlideDirection.Up) + fadeIn() with
                    slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
        }
    ) {
        content(it)
    }
}

@Composable
private fun NumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { newValue ->
            onValueChange(newValue.filter { it.isDigit() }.toInt())
        },
        singleLine = true,
        keyboardOptions = remember {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        },
        label = label
    )
}

@Composable
private fun <T : Enum<T>> ComboBox(
    values: Array<T>,
    value: T,
    onValueChange: (T) -> Unit,
    label: (@Composable () -> Unit)? = null
) {
    val (expanded, setExpanded) = remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded
    ) {
        OutlinedTextField(
            value = value.name,
            onValueChange = {},
            singleLine = true,
            label = label,
            readOnly = true,
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
            values.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = { onValueChange(it) }
                )
            }
        }
    }
}

private fun LazyGridScope.singleItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyGridItemScope.() -> Unit
) = item(
    key = key,
    span = { GridItemSpan(maxCurrentLineSpan) },
    contentType = contentType,
    content = content,
)