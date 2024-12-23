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
) : FormField<Int>(initial, id = label) {

    @Composable
    override fun invoke(modifier: Modifier) {
        OutlinedTextField(
            modifier = modifier,
            value = value.toString(),
            onValueChange = {
                val intRepr = it.toIntOrNull()
                if (intRepr != null) {
                    value = intRepr
                }
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

    @Suppress("ConvertTwoComparisonsToRangeCheck")
    override fun isValid(): Boolean {
        return value > min && value < max
    }

}