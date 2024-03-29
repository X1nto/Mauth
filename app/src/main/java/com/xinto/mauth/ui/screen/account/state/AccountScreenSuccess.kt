package com.xinto.mauth.ui.screen.account.state

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.core.contracts.PickVisualMediaPersistent
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.screen.account.component.AccountComboBox
import com.xinto.mauth.ui.screen.account.component.AccountDataField
import com.xinto.mauth.ui.screen.account.component.AccountNumberField
import java.util.UUID

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
    digits: String,
    onDigitsChange: (String) -> Unit,
    counter: String,
    onCounterChange: (String) -> Unit,
    period: String,
    onPeriodChange: (String) -> Unit,
) {
    val imageSelectLauncher = rememberLauncherForActivityResult(
        PickVisualMediaPersistent()
    ) {
        onIconChange(it)
    }
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2)
    ) {
        singleItem {
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
                                painter = painterResource(R.drawable.ic_add_a_photo),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
        singleItem {
            AccountDataField(
                value = label,
                onValueChange = onLabelChange,
                label = {
                    Text(stringResource(R.string.account_data_label))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_label),
                        contentDescription = null
                    )
                },
                required = true
            )
        }
        singleItem {
            AccountDataField(
                value = issuer,
                onValueChange = onIssuerChange,
                label = {
                    Text(stringResource(R.string.account_data_issuer))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_apartment),
                        contentDescription = null
                    )
                },
            )
        }
        singleItem {
            var secretShown by remember { mutableStateOf(false) }
            AccountDataField(
                value = secret,
                onValueChange = onSecretChange,
                label = {
                    Text(stringResource(R.string.account_data_secret))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_key),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { secretShown = !secretShown }) {
                        val visible = painterResource(R.drawable.ic_visibility)
                        val notVisible = painterResource(R.drawable.ic_visibility_off)
                        Icon(
                            painter = if (secretShown) visible else notVisible,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = remember(secretShown) {
                    if (secretShown) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                },
                keyboardOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Password) },
                required = true
            )
        }
        item {
            AccountComboBox(
                values = OtpType.entries,
                value = type,
                onValueChange = onTypeChange,
                label = {
                    Text(stringResource(R.string.account_data_type))
                }
            )
        }
        item {
            AccountComboBox(
                values = OtpDigest.entries,
                value = digest,
                onValueChange = onDigestChange,
                label = {
                    Text(stringResource(R.string.account_data_algorithm))
                }
            )
        }
        item {
            AccountNumberField(
                value = digits,
                onValueChange = onDigitsChange,
                label = {
                    Text(stringResource(R.string.account_data_digits))
                },
                min = 1,
                max = 10,
                supportingText = {
                    Text(stringResource(R.string.account_data_status_range, "1", "10"))
                }
            )
        }
        item {
            AnimatedContent(
                targetState = type,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() with
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
                },
                label = "HOTP/TOTP"
            ) {
                when (it) {
                    OtpType.TOTP -> {
                        AccountNumberField(
                            value = period,
                            onValueChange = onPeriodChange,
                            label = {
                                Text(stringResource(R.string.account_data_period))
                            },
                            min = 1,
                            max = Int.MAX_VALUE / 1000
                        )
                    }
                    OtpType.HOTP -> {
                        AccountNumberField(
                            value = counter,
                            onValueChange = onCounterChange,
                            label = {
                                Text(stringResource(R.string.account_data_counter))
                            }
                        )
                    }
                }
            }
        }
        if (id != null) {
            singleItem {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = id.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
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