package com.xinto.mauth.ui.screen.account.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import com.xinto.mauth.R

@Composable
fun AccountDataField(
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = if (required) { ->
            Text(stringResource(R.string.account_data_status_required))
        } else null,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
    )
}