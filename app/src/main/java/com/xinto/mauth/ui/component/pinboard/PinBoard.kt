package com.xinto.mauth.ui.component.pinboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.theme.MauthTheme

@OptIn(ExperimentalGridApi::class)
@Composable
fun PinBoard(
    modifier: Modifier = Modifier,
    horizontalButtonSpace: Dp = PinBoardDefaults.HorizontalButtonSpace,
    verticalButtonSpace: Dp = PinBoardDefaults.VerticalButtonSpace,
    minButtonSize: Dp = PinButtonDefaults.PinButtonNormalMinSize,
    maxButtonSize: Dp = PinBoardDefaults.MaxButtonSize,
    state: PinBoardState = rememberPinBoardState()
) {
    Grid(
        modifier = modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = maxButtonSize * 3 + horizontalButtonSpace * 2),
        config = {
            repeat(3) { column(1.fr) }
            gap(row = verticalButtonSpace, column = horizontalButtonSpace)
        }
    ) {
        state.buttons.forEach { button ->
            when (button) {
                is PinBoardState.PinBoardButton.Number -> {
                    PinButton(
                        onClick = { state.onNumberClick(button.number) },
                        minButtonSize = minButtonSize
                    ) {
                        Text(button.toString())
                    }
                }
                is PinBoardState.PinBoardButton.Backspace,
                is PinBoardState.PinBoardButton.Fingerprint,
                is PinBoardState.PinBoardButton.Enter -> {
                    PrimaryPinButton(
                        onClick = when (button) {
                            is PinBoardState.PinBoardButton.Backspace -> state.onBackspaceClick
                            is PinBoardState.PinBoardButton.Fingerprint -> state.onFingerprintClick
                            is PinBoardState.PinBoardButton.Enter -> state.onEnterClick
                        },
                        onLongClick =
                            if (button is PinBoardState.PinBoardButton.Backspace)
                                state.onBackspaceLongClick
                            else null,
                        longClickHaptic = PinBoardDefaults.ClearAllHaptic,
                        minButtonSize = minButtonSize
                    ) {
                        val iconRes = when (button) {
                            is PinBoardState.PinBoardButton.Backspace -> R.drawable.ic_backspace
                            is PinBoardState.PinBoardButton.Fingerprint -> R.drawable.ic_fingerprint
                            is PinBoardState.PinBoardButton.Enter -> R.drawable.ic_tab
                        }
                        Icon(
                            modifier = Modifier.fillMaxSize(0.4f),
                            painter = painterResource(iconRes),
                            contentDescription = null
                        )
                    }
                }
                is PinBoardState.PinBoardButton.Empty -> {
                    Spacer(Modifier)
                }
            }
        }
    }
}

object PinBoardDefaults {

    val MaxButtonSize = 92.dp
    val HorizontalButtonSpace = 14.dp
    val VerticalButtonSpace = 10.dp

    val ClearAllHaptic = HapticFeedbackType.Confirm
}

@Composable
fun rememberPinBoardState(
    showFingerprint: Boolean = false,
    showEnter: Boolean = false,
    onNumberClick: (Char) -> Unit = {},
    onBackspaceClick: () -> Unit = {},
    onBackspaceLongClick: () -> Unit = {},
    onEnterClick: () -> Unit = {},
    onFingerprintClick: () -> Unit = {},
): PinBoardState {
    return remember(
        showFingerprint,
        showEnter,
        onNumberClick,
        onBackspaceClick,
        onBackspaceLongClick,
        onEnterClick,
        onFingerprintClick,
    ) {
        PinBoardState(
            showFingerprint = showFingerprint,
            showEnter = showEnter,
            onNumberClick = onNumberClick,
            onBackspaceClick = onBackspaceClick,
            onBackspaceLongClick = onBackspaceLongClick,
            onEnterClick = onEnterClick,
            onFingerprintClick = onFingerprintClick
        )
    }
}

@Immutable
data class PinBoardState(
    val showFingerprint: Boolean,
    val showEnter: Boolean,
    val onNumberClick: (Char) -> Unit,
    val onBackspaceClick: () -> Unit,
    val onBackspaceLongClick: () -> Unit = {},
    val onEnterClick: () -> Unit,
    val onFingerprintClick: () -> Unit,
) {

    val buttons = buildList {
        ('1'..'9').forEach {
            add(PinBoardButton.Number(it))
        }

        if (showFingerprint) {
            add(PinBoardButton.Fingerprint)
        } else if (showEnter) {
            add(PinBoardButton.Backspace)
        } else {
            add(PinBoardButton.Empty)
        }

        add(PinBoardButton.Number('0'))

        if (showEnter) {
            add(PinBoardButton.Enter)
        } else {
            add(PinBoardButton.Backspace)
        }
    }

    sealed interface PinBoardButton {

        @JvmInline
        value class Number(val number: Char) : PinBoardButton {
            override fun toString() = number.toString()
        }

        data object Fingerprint : PinBoardButton
        data object Backspace : PinBoardButton
        data object Enter : PinBoardButton
        data object Empty : PinBoardButton
    }
}


@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_Plain() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(),
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithFingerprint() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(showFingerprint = true),
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithEnter() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(showEnter = true),
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithFingerprintAndEnter() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(
                    showFingerprint = true,
                    showEnter = true,
                ),
            )
        }
    }
}
