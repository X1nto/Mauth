package com.xinto.mauth.ui.screen.account

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.unit.dp
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
            DropdownMenuPopup(
                modifier = Modifier.exposedDropdownSize(),
                expanded = expanded,
                onDismissRequest = { setExpanded(false) }
            ) {
                val groupListScrollState = rememberScrollState()
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShapes(MenuDefaults.leadingGroupShape),
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .verticalScroll(groupListScrollState),
                ) {
                    val selected = value == null
                    DropdownMenuItem(
                        selected = selected,
                        onClick = {
                            setExpanded(false)
                            value = null
                        },
                        text = { Text(stringResource(R.string.account_data_group_none)) },
                        shapes = MenuDefaults.itemShapes(MenuDefaults.leadingItemShape),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_label_off),
                                contentDescription = null
                            )
                        },
                        trailingIcon = if (!selected) null else { ->
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null
                            )
                        }
                    )
                    if (groupList.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding))
                        groupList.forEachIndexed { index, group ->
                            val selected = value == group.id
                            DropdownMenuItem(
                                selected = selected,
                                onClick = {
                                    setExpanded(false)
                                    value = group.id
                                },
                                text = { Text(group.name) },
                                shapes = MenuDefaults.itemShape(
                                    index = index + 1,
                                    count = groupList.size + 1
                                ),
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
                                trailingIcon = if (!selected) null else { ->
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(MenuDefaults.GroupSpacing))
                DropdownMenuGroup(shapes = MenuDefaults.groupShapes(MenuDefaults.trailingGroupShape)) {
                    DropdownMenuItem(
                        selected = false,
                        onClick = {
                            setExpanded(false)
                            showCreateDialog = true
                        },
                        text = { Text(stringResource(R.string.groups_action_add_group)) },
                        shapes = MenuDefaults.itemShapes(MenuDefaults.selectedItemShape),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null
                            )
                        },
                    )
                }
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

