package com.xinto.mauth.ui.screen.account

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.R.drawable.ic_label
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.ui.component.form.FormField
import com.xinto.mauth.ui.screen.groups.CreateGroupDialog
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GroupFormField(
    initial: UUID?,
    private val groups: StateFlow<List<DomainGroup>>,
    private val onCreateGroup: suspend (name: String, emoji: String?) -> UUID
) : FormField<UUID?>(initial, id = R.string.account_data_group) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun invoke(modifier: Modifier) {
        val groupList by groups.collectAsStateWithLifecycle()
        val scope = rememberCoroutineScope()
        val (expanded, setExpanded) = remember { mutableStateOf(false) }
        var showCreateDialog by remember { mutableStateOf(false) }
        val selectedName = groupList.firstOrNull { it.id == value }?.name
            ?: stringResource(R.string.account_data_group_none)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = setExpanded
        ) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                value = selectedName,
                onValueChange = {},
                singleLine = true,
                readOnly = true,
                label = { Text(stringResource(R.string.account_data_group)) },
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
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.account_data_group_none)) },
                    onClick = {
                        setExpanded(false)
                        value = null
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_label_off),
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (value != null) null else { ->
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null
                        )
                    }
                )
                groupList.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name) },
                        onClick = {
                            setExpanded(false)
                            value = group.id
                        },
                        leadingIcon = {
                            if (group.emoji != null) {
                                Text(text = group.emoji)
                            } else {
                                Icon(
                                    painter = painterResource(ic_label),
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = if (value != group.id) null else { ->
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null
                            )
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.groups_action_add_group)) },
                    onClick = {
                        setExpanded(false)
                        showCreateDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null
                        )
                    }
                )
            }
        }
        if (showCreateDialog) {
            CreateGroupDialog(
                isNameTaken = { candidate -> groupList.any { it.name.equals(candidate, ignoreCase = true) } },
                onConfirm = { name, emoji ->
                    showCreateDialog = false
                    scope.launch { value = onCreateGroup(name, emoji) }
                },
                onDismissRequest = { showCreateDialog = false }
            )
        }
    }
}

