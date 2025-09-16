package com.xinto.mauth.ui.component.form

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.xinto.mauth.R

class PasswordFormField(
    initial: String,

    @param:StringRes
    private val label: Int,

    @param:DrawableRes
    private val icon: Int,
    private val required: Boolean = false
) : FormField<String>(initial, id = label) {

    @Composable
    override fun invoke(modifier: Modifier) {
        var showPassword by rememberSaveable { mutableStateOf(false) }
        val visualTransformation = remember(showPassword) {
            if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            }
        }
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
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    val visible = painterResource(R.drawable.ic_visibility)
                    val notVisible = painterResource(R.drawable.ic_visibility_off)
                    Icon(
                        painter = if (showPassword) visible else notVisible,
                        contentDescription = null
                    )
                }
            },
            supportingText = if (!required) null else { ->
                Text(stringResource(R.string.account_data_status_required))
            },
            visualTransformation = visualTransformation,
            isError = error
        )
    }

    override fun isValid(): Boolean {
        if (!required) return true

        return value.isNotEmpty()
    }

}