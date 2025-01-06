package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.component.TwoPaneCard
import com.xinto.mauth.ui.component.UriImage

@Composable
fun HomeAccountCard(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onCounterClick: () -> Unit,
    onCopyCode: (visible: Boolean) -> Unit,
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
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        IconButton(onClick = onEdit) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = null
                            )
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
                    painter = painterResource(R.drawable.ic_visibility),
                    contentDescription = null
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_visibility_off),
                    contentDescription = null
                )
            }
        }
        FilledTonalIconButton(onClick = onCopyCode) {
            Icon(
                painter = painterResource(R.drawable.ic_copy_all),
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