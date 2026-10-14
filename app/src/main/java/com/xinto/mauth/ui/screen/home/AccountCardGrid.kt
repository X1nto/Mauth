package com.xinto.mauth.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toPath
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.TransformResult
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.component.TwoPaneCard
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID
import kotlin.math.sqrt

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
                RefreshButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    count = realtimeData.count,
                    onClick = onCounterClick
                )
            }
            is DomainOtpRealtimeData.Totp -> {
                MorphingTimer(
                    countdown = realtimeData.countdown,
                    progress = realtimeData.progress
                )
            }
        }
        OtpCode(
            code = realtimeData.code,
            visible = showCode,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

private val morphsByTime = List(30) {
    val sides = it + 1

    if (sides >= 6) {
        // Cookie12Sided's depth at 12, shallower above so high counts don't look like gears, deeper below.
        val depth = 0.2f * sqrt(12f / sides)
        return@List RoundedPolygon.star(
            numVerticesPerRadius = sides,
            radius = 0.5f,
            innerRadius = 0.5f * (1f - depth),
            rounding = CornerRounding(0.25f),
            centerX = 0.5f,
            centerY = 0.5f,
        )
    }

    if (sides >= 3) {
        val radius = when (sides) {
            3 -> 0.6f
            4 -> 0.5f
            else -> 0.46f
        }
        return@List RoundedPolygon(
            numVertices = sides,
            radius = radius,
            centerX = 0.5f,
            centerY = 0.5f,
            rounding = CornerRounding(0.14f),
        )
    }

    if (sides == 2) {
        return@List RoundedPolygon.circle(
            numVertices = 10,
            radius = 0.47f,
            centerX = 0.5f,
            centerY = 0.5f
        ).transformed { x, y ->
            val dx = x - 0.5f
            val dy = (y - 0.5f) * 0.72f
            TransformResult(0.5f + (dx - dy) * 0.7071f, 0.5f + (dx + dy) * 0.7071f)
        }
    }

    return@List RoundedPolygon.circle(
        numVertices = 10,
        radius = 0.39f,
        centerX = 0.5f,
        centerY = 0.5f
    )
}.zipWithNext(::Morph)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MorphingTimer(
    countdown: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer
    val sides by animateFloatAsState(
        targetValue = countdown.coerceIn(1, 30).toFloat(),
        animationSpec = MotionScheme.expressive().defaultSpatialSpec(),
    )
    val urgency = { (6f - sides).coerceIn(0f, 1f) }
    val containerColor = ColorProducer {
        lerp(primaryContainer, errorContainer, urgency())
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
            .drawWithCache {
                val path = Path()
                val shapeSize = 38.dp.toPx()
                val matrix = Matrix().apply {
                    translate(
                        x = (size.width - shapeSize) / 2f,
                        y = (size.height - shapeSize) / 2f
                    )
                    scale(shapeSize, shapeSize)
                }
                onDrawBehind {
                    path.apply {
                        val morph = (sides.toInt() - 1).coerceIn(0, morphsByTime.lastIndex)
                        morphsByTime[morph].toPath(
                            progress = (sides - 1 - morph).coerceIn(0f, 1f),
                            path = this
                        )
                        transform(matrix)
                    }
                    drawCircle(containerColor())
                    val turnDegrees = if (sides >= 5f) {
                        -18f - 360f * ((sides - 5f) / 25f).coerceAtMost(1f)
                    } else {
                        -90f + 360f / sides.coerceAtLeast(1f)
                    }
                    rotate(turnDegrees) {
                        drawPath(path, lerp(onPrimaryContainer, onErrorContainer, urgency()))
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            // Keeps long countdowns (periods past 99s) inside the shape.
            modifier = Modifier.padding(horizontal = 12.dp),
            text = countdown.toString(),
            color = containerColor,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = MaterialTheme.typography.labelLarge.fontSize,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val turns by animateFloatAsState(
        targetValue = count.toFloat(),
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
    )
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(text = stringResource(R.string.home_action_counter_increase)) } },
        state = rememberTooltipState(),
    ) {
        FilledIconButton(
            onClick = onClick,
            shapes = IconButtonDefaults.shapes(
                shape = IconButtonDefaults.smallSquareShape,
                pressedShape = IconButtonDefaults.smallPressedShape,
            ),
            modifier = Modifier.size(IconButtonDefaults.smallContainerSize()),
        ) {
            Icon(
                modifier = Modifier.graphicsLayer {
                    rotationZ = turns % 1f * 360f
                },
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.home_action_counter_increase),
            )
        }
    }
}
