package com.xinto.mauth.ui.component.form

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.xinto.mauth.R

class IntFormField(
    initial: Int,

    @StringRes
    private val label: Int,

    private val min: Int = Int.MIN_VALUE,
    private val max: Int = Int.MAX_VALUE
) : FormField<String>(initial.toString(), id = label) {

    @Composable
    override fun invoke(modifier: Modifier) {
        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = {
                value = it
            },
            label = {
                Text(stringResource(label))
            },
            supportingText = if (max == Int.MAX_VALUE) null else { ->
                Text(stringResource(R.string.account_data_status_range, min.toString(), max.toString()))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error
        )
    }

    override fun isValid(): Boolean {
        val intValue = value.toIntOrNull() ?: return false

        return intValue in min..max
    }

}