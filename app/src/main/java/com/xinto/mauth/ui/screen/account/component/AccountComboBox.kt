package com.xinto.mauth.ui.screen.account.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.xinto.mauth.R
import kotlin.enums.EnumEntries

@Composable
fun <E : Enum<E>> AccountComboBox(
    values: EnumEntries<E>,
    value: E,
    onValueChange: (E) -> Unit,
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
            modifier = Modifier.menuAnchor(),
            value = value.name,
            onValueChange = {},
            singleLine = true,
            label = label,
            readOnly = true,
            trailingIcon = {
                val iconRotation by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    modifier = Modifier.rotate(iconRotation),
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
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
                    onClick = {
                        setExpanded(false)
                        onValueChange(it)
                    }
                )
            }
        }
    }
}
