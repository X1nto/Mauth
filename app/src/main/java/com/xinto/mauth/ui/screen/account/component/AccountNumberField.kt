package com.xinto.mauth.ui.screen.account.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AccountNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    min: Int = 0,
    max: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = remember {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        },
        supportingText = supportingText,
        label = label,
        isError = value.toIntOrNull() == null || (value.toInt() < min || value.toInt() > max)
    )
}