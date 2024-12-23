package com.xinto.mauth.ui.component.form

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

class TextFormField(
    initial: String,

    @StringRes
    private val label: Int,

    @DrawableRes
    private val icon: Int = 0,
    private val required: Boolean = false
) : FormField<String>(initial, id = label) {

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
            leadingIcon = if (icon == 0) null else { ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null
                )
            },
            supportingText = if (!required) null else { ->
                Text(stringResource(R.string.account_data_status_required))
            },
            isError = error
        )
    }

    override fun isValid(): Boolean {
        if (!required) return true

        return value.isNotEmpty()
    }


}