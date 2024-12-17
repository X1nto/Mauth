package com.xinto.mauth.ui.component.pinboard

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xinto.mauth.ui.theme.MauthTheme

@Composable
fun PinDisplay(
    length: Int,
    modifier: Modifier = Modifier,
    error: Boolean = false,
) {
    val inspectionMode = LocalInspectionMode.current
    val color = when (error) {
        true -> MaterialTheme.colorScheme.errorContainer
        false -> MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 8.dp)
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..<length) {
                val transitionState = remember(i, inspectionMode) {
                    if (inspectionMode) {
                        MutableTransitionState(true)
                    } else {
                        MutableTransitionState(false).apply {
                            targetState = true
                        }
                    }
                }
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = EnterTransition.None, // No animations
                    exit = ExitTransition.None
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinDisplay_Plain() {
    MauthTheme {
        PinDisplay(
            modifier = Modifier.width(200.dp),
            length = 4
        )
    }
}