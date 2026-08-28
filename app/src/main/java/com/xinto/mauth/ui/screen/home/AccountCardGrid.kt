package com.xinto.mauth.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.component.TwoPaneCard
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Composable
fun AccountCardGrid(
    accounts: ImmutableList<DomainAccount>,
    accountRealtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
    selectedAccounts: SnapshotStateList<UUID>,
    onAccountSelect: (UUID) -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    showCodesByDefault: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
    selectionEnabled: Boolean = true,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: CardElevation = CardDefaults.elevatedCardElevation(),
) {
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        columns = GridCells.Adaptive(minSize = 250.dp),
    ) {
        items(items = accounts, key = { it.id }) { account ->
            val realtimeData = accountRealtimeData[account.id]
            if (realtimeData != null) {
                Account(
                    onClick = {
                        if (selectionEnabled && selectedAccounts.isNotEmpty()) {
                            onAccountSelect(account.id)
                        }
                    },
                    onLongClick = {
                        if (selectionEnabled) {
                            onAccountSelect(account.id)
                        }
                    },
                    onEdit = { onAccountEdit(account.id) },
                    onCounterClick = { onAccountCounterIncrease(account.id) },
                    onCopyCode = { onAccountCopyCode(account.label, realtimeData.code, it) },
                    account = account,
                    realtimeData = realtimeData,
                    selected = selectedAccounts.contains(account.id),
                    selectionActive = selectedAccounts.isNotEmpty(),
                    showCodesByDefault = showCodesByDefault,
                    colors = colors,
                    elevation = elevation
                )
            }
        }
    }
}

private enum class CardTrailing { Edit, Checked, None }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Account(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onCounterClick: () -> Unit,
    onCopyCode: (visible: Boolean) -> Unit,
    account: DomainAccount,
    realtimeData: DomainOtpRealtimeData,
    selected: Boolean,
    selectionActive: Boolean,
    showCodesByDefault: Boolean,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
) {
    var showCode by remember(showCodesByDefault) { mutableStateOf(showCodesByDefault) }
    TwoPaneCard(
        onClick = onClick,
        onLongClick = onLongClick,
        selected = selected,
        expanded = !selectionActive,
        topContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccountAvatar(account = account)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (account.issuer.isNotBlank()) {
                        Text(
                            text = account.issuer,
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalContentColor.current.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = account.label,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                val target1 = when {
                    !selectionActive -> CardTrailing.Edit
                    selected -> CardTrailing.Checked
                    else -> CardTrailing.None
                }
                val spatial1 = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
                AnimatedContent(
                    targetState = target1,
                    transitionSpec = {
                        scaleIn(spatial1) + fadeIn() togetherWith scaleOut(spatial1) + fadeOut()
                    },
                    contentAlignment = Alignment.Center,
                    label = "AccountCardTrailing",
                ) { trailingState ->
                    when (trailingState) {
                        CardTrailing.Edit -> {
                            val editLabel = stringResource(R.string.home_action_edit)
                            TooltipBox(
                                modifier = Modifier,
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Below
                                ),
                                tooltip = { PlainTooltip { Text(text = editLabel) } },
                                state = rememberTooltipState(),
                            ) {
                                IconButton(onClick = onEdit) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = editLabel
                                    )
                                }
                            }
                        }

                        CardTrailing.Checked -> {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = MaterialShapes.Cookie9Sided.toShape(),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(20.dp),
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        CardTrailing.None -> Spacer(Modifier.size(48.dp))
                    }
                }
            }
        },
        bottomContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RealtimeInformation(
                    realtimeData = realtimeData,
                    showCode = showCode,
                    onCounterClick = onCounterClick
                )
                InteractionButtons(
                    showCode = showCode,
                    onShowCodeChange = { showCode = it },
                    onCopyCode = { onCopyCode(showCode) }
                )
            }
        },
        colors = colors,
        elevation = elevation,
        border = border
    )
}

@Composable
private fun InteractionButtons(
    showCode: Boolean,
    onShowCodeChange: (Boolean) -> Unit,
    onCopyCode: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TooltipBox(
            modifier = Modifier,
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = {
                PlainTooltip {
                    val labelRes = if (showCode) R.string.home_action_code_hide else R.string.home_action_code_show
                    Text(text = stringResource(labelRes))
                }
            },
            state = rememberTooltipState(),
            content = {
                FilledIconToggleButton(
                    checked = showCode,
                    onCheckedChange = onShowCodeChange,
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.tertiary,
                        checkedContentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    if (showCode) {
                        Icon(
                            painter = painterResource(R.drawable.ic_visibility),
                            contentDescription = stringResource(R.string.home_action_code_show)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_visibility_off),
                            contentDescription = stringResource(R.string.home_action_code_hide)
                        )
                    }
                }
            },
        )
        val copyLabel = stringResource(R.string.home_action_code_copy)
        TooltipBox(
            modifier = Modifier,
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { PlainTooltip { Text(text = copyLabel) } },
            state = rememberTooltipState(),
            content = {
                FilledTonalIconButton(onClick = onCopyCode) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy_all),
                        contentDescription = copyLabel
                    )
                }
            },
        )
    }
}

@Composable
private fun RealtimeInformation(
    realtimeData: DomainOtpRealtimeData,
    showCode: Boolean,
    onCounterClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (realtimeData) {
            is DomainOtpRealtimeData.Hotp -> {
                FilledTonalIconButton(onClick = onCounterClick) {
                    Text(realtimeData.count.toString())
                }
            }
            is DomainOtpRealtimeData.Totp -> {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress by animateFloatAsState(
                        targetValue = realtimeData.progress,
                        animationSpec = tween(500)
                    )
                    CircularProgressIndicator(progress = { progress })
                    Text(realtimeData.countdown.toString())
                }
            }
        }
        OtpCode(
            code = realtimeData.code,
            visible = showCode,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

