package com.xinto.mauth.ui.screen.groups

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.xinto.mauth.domain.account.model.DomainAccount
import kotlinx.collections.immutable.ImmutableMap
import java.util.UUID
import kotlin.math.abs

@Stable
class AccountDragHandler(private val lazyListState: LazyListState) {

    var keyTargets: Map<String, AccountDropTarget> = emptyMap()

    var onMove: (UUID, UUID?) -> Unit = { _, _ -> }

    var draggedAccount by mutableStateOf<DomainAccount?>(null)
        private set

    var pointerY by mutableFloatStateOf(0f)
        private set

    var grabOffsetY by mutableFloatStateOf(0f)
        private set

    var hoveredTarget by mutableStateOf<AccountDropTarget?>(null)
        private set

    private var origin: UUID? = null

    val isDragging: Boolean get() = draggedAccount != null

    fun isDragging(accountId: UUID): Boolean = draggedAccount?.id == accountId

    fun isOver(groupId: UUID?): Boolean = hoveredTarget == AccountDropTarget(groupId)

    fun handleFor(account: DomainAccount, groupId: UUID?): Modifier =
        Modifier.pointerInput(account.id, groupId) {
            detectDragGestures(
                onDragStart = {
                    val info = lazyListState.layoutInfo.visibleItemsInfo
                        .firstOrNull { it.key == accountRowKey(account.id) }
                    val itemTop = info?.offset?.toFloat() ?: 0f
                    val itemHeight = (info?.size ?: 0).toFloat()
                    grabOffsetY = itemHeight / 2f
                    pointerY = itemTop + itemHeight / 2f
                    origin = groupId
                    hoveredTarget = AccountDropTarget(groupId)
                    draggedAccount = account
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    pointerY += dragAmount.y
                    hoveredTarget = hitTest(pointerY)
                },
                onDragEnd = {
                    val target = hoveredTarget
                    val account0 = draggedAccount
                    val origin0 = origin
                    draggedAccount = null
                    hoveredTarget = null
                    if (account0 != null && target != null && target.groupId != origin0) {
                        onMove(account0.id, target.groupId)
                    }
                },
                onDragCancel = {
                    draggedAccount = null
                    hoveredTarget = null
                }
            )
        }

    fun refreshHoveredTarget() {
        if (draggedAccount != null) {
            hoveredTarget = hitTest(pointerY)
        }
    }

    private fun hitTest(y: Float): AccountDropTarget? {
        val items = lazyListState.layoutInfo.visibleItemsInfo
        if (items.isEmpty()) return null
        val hit = items.firstOrNull { y >= it.offset && y < it.offset + it.size }
            ?: items.minByOrNull { abs((it.offset + it.size / 2f) - y) }
            ?: return null
        return keyTargets[hit.key as? String ?: return null]
    }
}

@Composable
fun rememberAccountDragHandler(
    lazyListState: LazyListState,
    keyTargets: ImmutableMap<String, AccountDropTarget>,
    onMove: (UUID, UUID?) -> Unit
): AccountDragHandler {
    val handler = remember(lazyListState) { AccountDragHandler(lazyListState) }
    handler.keyTargets = keyTargets
    handler.onMove = onMove
    return handler
}
