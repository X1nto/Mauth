package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.domain.model.shortLabel
import com.xinto.mauth.domain.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.component.TwoPaneCard
import com.xinto.mauth.ui.component.UriImage

@Composable
fun HomeAccountCard(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onCounterClick: () -> Unit,
    onCopyCode: () -> Unit,
    account: DomainAccount,
    realtimeData: DomainOtpRealtimeData,
    selected: Boolean,
) {
    var showCode by remember { mutableStateOf(false) }
    TwoPaneCard(
        selected = selected,
        expanded = !selected,
        topContent = {
            AccountInfo(
                icon = {
                    if (account.icon != null) {
                        UriImage(uri = account.icon!!)
                    } else {
                        Text(account.shortLabel, style = MaterialTheme.typography.titleLarge)
                    }
                },
                name = { Text(account.label) },
                issuer = {
                    if (account.issuer != "") {
                        Text(account.issuer)
                    }
                },
                trailing = {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        },
        bottomContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RealtimeInformation(
                    realtimeData = realtimeData,
                    showCode = showCode,
                    onCounterClick = onCounterClick
                )
                Spacer(modifier = Modifier.weight(1f))
                InteractionButtons(
                    showCode = showCode,
                    onShowCodeChange = {
                        showCode = it
                    },
                    onCopyCode = onCopyCode
                )
            }
        },
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@Composable
private fun InteractionButtons(
    showCode: Boolean,
    onShowCodeChange: (Boolean) -> Unit,
    onCopyCode: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconToggleButton(
            checked = showCode, 
            onCheckedChange = onShowCodeChange
        ) {
            if (showCode) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.VisibilityOff,
                    contentDescription = null
                )
            }
        }
        FilledTonalIconButton(onClick = onCopyCode) {
            Icon(
                imageVector = Icons.Rounded.CopyAll,
                contentDescription = null
            )
        }
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
                    CircularProgressIndicator(progress)
                    Text(realtimeData.countdown.toString())
                }
            }
        }
        AnimatedContent(
            targetState = code,
            transitionSpec = {
                if (initialState.first == targetState.first) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() with
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeIn() with
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
            shape = MaterialTheme.shapes.medium,
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
        Spacer(modifier = Modifier.weight(1f))
        trailing()
    }
}