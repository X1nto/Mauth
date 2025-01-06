package com.xinto.mauth.ui.component.form

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

class ComboBoxFormField<E: Enum<E>>(
    initial: E,

    @StringRes
    private val label: Int
) : FormField<E>(initial, id = label) {

    private val clazz = initial.declaringJavaClass

    @Composable
    override fun invoke(modifier: Modifier) {
        val (expanded, setExpanded) = remember {
            mutableStateOf(false)
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = setExpanded
        ) {
            OutlinedTextField(
                modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                value = value.name,
                onValueChange = {},
                singleLine = true,
                label = {
                    Text(stringResource(label))
                },
                readOnly = true,
                trailingIcon = {
                    val iconRotation by animateFloatAsState(if (expanded) 180f else 0f)
                    Icon(
                        modifier = Modifier.rotate(iconRotation),
                        painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                        contentDescription = null
                    )
                },
                isError = error
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { setExpanded(false) }
            ) {
                clazz.enumConstants!!.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            setExpanded(false)
                            value = it
                        }
                    )
                }
            }
        }
    }
}