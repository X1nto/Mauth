package com.xinto.mauth.ui.component.form

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

class PasswordFormField(
    initial: String,

    @param:StringRes
    private val label: Int,

    @param:DrawableRes
    private val icon: Int,
    private val required: Boolean = false
) : FormField<String>(initial, id = label) {

    private val fieldState = TextFieldState(initial)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun invoke(modifier: Modifier) {
        var showPassword by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(fieldState) {
            snapshotFlow { fieldState.text.toString() }.collect {
                value = it
            }
        }
        OutlinedSecureTextField(
            modifier = modifier,
            state = fieldState,
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
                val toggleLabel = stringResource(
                    if (showPassword) R.string.account_action_secret_hide else R.string.account_action_secret_show
                )
                TooltipBox(
                    modifier = Modifier,
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                    tooltip = { this.PlainTooltip { Text(text = toggleLabel) } },
                    state = rememberTooltipState(),
                    content = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            val visible = painterResource(R.drawable.ic_visibility)
                            val notVisible = painterResource(R.drawable.ic_visibility_off)
                            Icon(
                                painter = if (showPassword) visible else notVisible,
                                contentDescription = toggleLabel
                            )
                        }
                    },
                )
            },
            supportingText = if (!required) null else { ->
                Text(stringResource(R.string.account_data_status_required))
            },
            textObfuscationMode = if (showPassword) TextObfuscationMode.Visible else TextObfuscationMode.System,
            isError = error
        )
    }

    override fun isValid(): Boolean {
        if (!required) return true

        return value.isNotEmpty()
    }

}