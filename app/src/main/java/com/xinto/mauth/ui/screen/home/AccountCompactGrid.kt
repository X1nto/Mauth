@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Composable
fun AccountCompactGrid(
    accounts: ImmutableList<DomainAccount>,
    accountRealtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
    selectedAccounts: SnapshotStateList<UUID>,
    onAccountSelect: (UUID) -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    showCodesByDefault: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(top = 8.dp, bottom = 88.dp),
    selectionEnabled: Boolean = true,
) {
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = contentPadding,
        columns = GridCells.Adaptive(minSize = 400.dp),
    ) {
        items(items = accounts, key = { it.id }) { account ->
            val realtimeData = accountRealtimeData[account.id]
            if (realtimeData != null) {
                Account(
                    onSelectToggle = { onAccountSelect(account.id) },
                    onEdit = { onAccountEdit(account.id) },
                    onCounterClick = { onAccountCounterIncrease(account.id) },
                    onCopyCode = { onAccountCopyCode(account.label, realtimeData.code, it) },
                    account = account,
                    realtimeData = realtimeData,
                    selected = selectedAccounts.contains(account.id),
                    selectionActive = selectedAccounts.isNotEmpty(),
                    showCodesByDefault = showCodesByDefault,
                    selectionEnabled = selectionEnabled,
                )
            }
        }
    }
}


private enum class CompactTrailing { Actions, Checked, None }

@Composable
private fun Account(
    onSelectToggle: () -> Unit,
    onEdit: () -> Unit,
    onCounterClick: () -> Unit,
    onCopyCode: (visible: Boolean) -> Unit,
    account: DomainAccount,
    realtimeData: DomainOtpRealtimeData,
    selected: Boolean,
    selectionActive: Boolean,
    showCodesByDefault: Boolean,
    modifier: Modifier = Modifier,
    selectionEnabled: Boolean = true,
) {
    var showCode by remember(showCodesByDefault) { mutableStateOf(showCodesByDefault) }
    val urgent = realtimeData is DomainOtpRealtimeData.Totp && realtimeData.countdown <= 5
    val codeColor by animateColorAsState(
        targetValue = if (urgent) MaterialTheme.colorScheme.error else LocalContentColor.current,
        label = "OtpCodeColor",
    )
    val avatarContainerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        label = "AccountAvatarContainer",
    )
    val avatarContentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        label = "AccountAvatarContent",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "AccountListItemContainer",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .combinedClickable(
                onClick = {
                    if (selectionEnabled && selectionActive) {
                        onSelectToggle()
                    } else {
                        onCopyCode(showCode)
                    }
                },
                onLongClick = if (selectionEnabled) onSelectToggle else null,
            )
            .heightIn(min = 64.dp)
            .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccountAvatar(
            account = account,
            containerColor = avatarContainerColor,
            contentColor = avatarContentColor,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (account.issuer.isNotBlank()) {
                Text(
                    text = account.issuer,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }
            Text(
                text = account.label,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        val spatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
        AnimatedContent(
            targetState = when {
                !selectionActive -> CompactTrailing.Actions
                selected -> CompactTrailing.Checked
                else -> CompactTrailing.None
            },
            transitionSpec = {
                scaleIn(spatial) + fadeIn() togetherWith scaleOut(spatial) + fadeOut()
            },
            contentAlignment = Alignment.CenterEnd,
            label = "AccountListItemTrailing",
        ) { trailingState ->
            when (trailingState) {
                CompactTrailing.Actions -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OtpCode(
                            modifier = Modifier.padding(end = 4.dp),
                            code = realtimeData.code,
                            visible = showCode,
                            progress = when (realtimeData) {
                                is DomainOtpRealtimeData.Totp -> rememberDrainProgress(realtimeData.progress)
                                is DomainOtpRealtimeData.Hotp -> null
                            },
                            color = codeColor,
                        )
                        MoreActions(
                            showCode = showCode,
                            onShowCodeChange = { showCode = it },
                            onEdit = onEdit,
                            onCounterClick = onCounterClick.takeIf { realtimeData is DomainOtpRealtimeData.Hotp },
                        )
                    }
                }
                CompactTrailing.Checked -> {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(20.dp),
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null
                            )
                        }
                    }
                }
                CompactTrailing.None -> Spacer(Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun rememberDrainProgress(target: Float): () -> Float {
    val progress = remember { Animatable(target) }
    LaunchedEffect(target) {
        if (target > progress.value) {
            progress.snapTo(target)
        } else {
            progress.animateTo(target, tween(durationMillis = 1000, easing = LinearEasing))
        }
    }
    return remember(progress) { { progress.value } }
}

@Composable
private fun MoreActions(
    showCode: Boolean,
    onShowCodeChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onCounterClick: (() -> Unit)? = null,
) {
    TooltipBox(
        modifier = Modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(text = stringResource(R.string.home_action_more)) } },
        state = rememberTooltipState()
    ) {
        var isMenuVisible by remember { mutableStateOf(false) }
        IconButton(
            modifier = modifier.size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)),
            onClick = { isMenuVisible = true },
            shapes = IconButtonDefaults.shapes(shape = IconButtonDefaults.smallSquareShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.home_more_options),
            )
            DropdownMenuPopup(
                expanded = isMenuVisible,
                onDismissRequest = { isMenuVisible = false },
            ) {
                val itemCount = if (onCounterClick != null) 3 else 2
                DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                    if (onCounterClick != null) {
                        DropdownMenuItem(
                            onClick = {
                                isMenuVisible = false
                                onCounterClick()
                            },
                            text = { Text(stringResource(R.string.home_action_counter_increase)) },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                    painter = painterResource(R.drawable.ic_refresh),
                                    contentDescription = null,
                                )
                            },
                            shape = MenuDefaults.itemShape(index = 0, count = itemCount).shape,
                        )
                    }

                    DropdownMenuItem(
                        onClick = {
                            isMenuVisible = false
                            onShowCodeChange(!showCode)
                        },
                        text = {
                            val labelRes = if (showCode) R.string.home_action_code_hide else R.string.home_action_code_show
                            Text(stringResource(labelRes))
                        },
                        leadingIcon = {
                            val painterRes = if (showCode) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                            Icon(
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                painter = painterResource(painterRes),
                                contentDescription = null,
                            )
                        },
                        shape = MenuDefaults.itemShape(index = itemCount - 2, count = itemCount).shape,
                    )
                    DropdownMenuItem(
                        onClick = {
                            isMenuVisible = false
                            onEdit()
                        },
                        text = { Text(stringResource(R.string.home_action_edit)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = null,
                            )
                        },
                        shape = MenuDefaults.itemShape(index = itemCount - 1, count = itemCount).shape,
                    )
                }
            }
        }
    }
}
