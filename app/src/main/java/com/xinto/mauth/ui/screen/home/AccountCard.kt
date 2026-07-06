package com.xinto.mauth.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.component.TwoPaneCard
import com.xinto.mauth.ui.component.UriImage

private enum class AccountCardTrailing { Edit, Checked, None }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountCard(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onCounterClick: () -> Unit,
    onCopyCode: (visible: Boolean) -> Unit,
    account: DomainAccount,
    realtimeData: DomainOtpRealtimeData,
    selected: Boolean,
    selectionActive: Boolean,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
) {
    var showCode by remember { mutableStateOf(false) }
    TwoPaneCard(
        onClick = onClick,
        onLongClick = onLongClick,
        selected = selected,
        expanded = !selectionActive,
        topContent = {
            AccountInfo(
                icon = {
                    if (account.icon != null) {
                        UriImage(uri = account.icon!!)
                    } else {
                        Text(
                            text = account.shortLabel,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            softWrap = false,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 10.sp,
                                maxFontSize = MaterialTheme.typography.titleLarge.fontSize,
                            )
                        )
                    }
                },
                iconShape = if (account.icon != null) MaterialTheme.shapes.medium else MaterialShapes.Cookie4Sided.toShape(),
                name = {
                    Text(
                        text = account.label,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                issuer = {
                    if (account.issuer != "") {
                        Text(account.issuer)
                    }
                },
                trailing = {
                    val target = when {
                        !selectionActive -> AccountCardTrailing.Edit
                        selected -> AccountCardTrailing.Checked
                        else -> AccountCardTrailing.None
                    }
                    val spatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
                    AnimatedContent(
                        targetState = target,
                        transitionSpec = {
                            scaleIn(spatial) + fadeIn() togetherWith scaleOut(spatial) + fadeOut()
                        },
                        contentAlignment = Alignment.Center,
                        label = "AccountCardTrailing",
                    ) { trailingState ->
                        when (trailingState) {
                            AccountCardTrailing.Edit -> {
                                val editLabel = stringResource(R.string.home_action_edit)
                                TooltipBox(
                                    modifier = Modifier,
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                    tooltip = { this.PlainTooltip { Text(text = editLabel) } },
                                    state = rememberTooltipState(),
                                    content = {
                                        IconButton(onClick = onEdit) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_edit),
                                                contentDescription = editLabel
                                            )
                                        }
                                    },
                                )
                            }
                            AccountCardTrailing.Checked -> {
                                Surface(
                                    modifier = Modifier.padding(end = 4.dp),
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
                            AccountCardTrailing.None -> {}
                        }
                    }
                }
            )
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
                    onShowCodeChange = {
                        showCode = it
                    },
                    onCopyCode = {
                        onCopyCode(showCode)
                    }
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
        val codeToggleLabel = stringResource(
            if (showCode) R.string.home_action_code_hide else R.string.home_action_code_show
        )
        TooltipBox(
            modifier = Modifier,
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { this.PlainTooltip { Text(text = codeToggleLabel) } },
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
                            contentDescription = codeToggleLabel
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_visibility_off),
                            contentDescription = codeToggleLabel
                        )
                    }
                }
            },
        )
        val copyLabel = stringResource(R.string.home_action_code_copy)
        TooltipBox(
            modifier = Modifier,
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { this.PlainTooltip { Text(text = copyLabel) } },
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
    val code = remember(showCode, realtimeData.code) {
        Pair(showCode, realtimeData.code)
    }
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
                    CircularProgressIndicator(
                        progress = { progress },
                    )
                    Text(realtimeData.countdown.toString())
                }
            }
        }
        AnimatedContent(
            targetState = code,
            transitionSpec = {
                if (initialState.first == targetState.first) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeIn() togetherWith
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
                }
            },
            label = "Code"
        ) { (show, code) ->
            val showAwareCode = if (show) code else "•".repeat(code.length)
            Text(showAwareCode, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun AccountInfo(
    icon: @Composable () -> Unit,
    iconShape: Shape,
    name: @Composable () -> Unit,
    issuer: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = iconShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.labelMedium,
                LocalContentColor provides LocalContentColor.current.copy(alpha = 0.7f)
            ) {
                issuer()
            }
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMedium,
                content = name,
            )
        }
        trailing()
    }
}