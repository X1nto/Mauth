package com.xinto.mauth.ui.component

import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

fun Modifier.brighten(): Modifier = this then BrightenElement

private data object BrightenElement : ModifierNodeElement<BrightenNode>() {
    override fun create() = BrightenNode()

    override fun update(node: BrightenNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "maxScreenBrightness"
    }
}

private class BrightenNode : Modifier.Node(), CompositionLocalConsumerModifierNode, ObserverModifierNode {

    private var window: Window? = null
    private var incomingBrightness: Float? = null

    override fun onAttach() = syncWindow()

    override fun onObservedReadsChanged() = syncWindow()

    override fun onDetach() = restore()

    private fun syncWindow() {
        var next: Window? = null
        observeReads {
            next = currentValueOf(LocalView).findDialogWindow()
                ?: currentValueOf(LocalActivity)?.window
        }

        if (next === window)
            return

        restore()
        window = next
        incomingBrightness = next?.attributes?.screenBrightness
        next?.setBrightness(1f)
    }

    private fun restore() {
        val window = window
        val previous = incomingBrightness
        if (window != null && previous != null)
            window.setBrightness(previous)
        this.window = null
        this.incomingBrightness = null
    }
}

private fun Window.setBrightness(value: Float) {
    attributes = attributes.apply { screenBrightness = value }
}

private fun View.findDialogWindow(): Window? {
    var parent: ViewParent? = this.parent
    while (parent != null) {
        if (parent is DialogWindowProvider)
            return parent.window

        parent = parent.parent
    }

    return null
}