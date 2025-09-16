package com.xinto.mauth.ui.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.screen.home.HomeAddAccountMenu
import kotlinx.coroutines.launch

@Composable
fun HomeAddAccountSheet(
    onDismiss: () -> Unit,
    onAddAccountNavigate: (HomeAddAccountMenu) -> Unit
) {
    MaterialBottomSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_addaccount_title)) },
        subtitle = { Text(stringResource(R.string.home_addaccount_subtitle)) },
    ) {
        Column(
            modifier = Modifier.clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            HomeAddAccountMenu.entries.forEach { menu ->
                FullWidthButton(
                    onClick = { onAddAccountNavigate(menu) },
                    text = { Text(stringResource(menu.title)) },
                    icon = {
                        Icon(
                            painter = painterResource(menu.icon),
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FullWidthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                text()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialBottomSheetDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutine = rememberCoroutineScope()
    DisposableEffect(Unit) {
        coroutine.launch {
            state.show()
        }
        onDispose {
            coroutine.launch {
                state.hide()
            }
        }
    }
    val insets = WindowInsets.navigationBars
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                )
                .windowInsetsPadding(insets),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.headlineMedium) {
                title()
            }
            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                subtitle()
            }
            Box(modifier = Modifier.padding(top = 12.dp)) {
                body()
            }
        }
    }
}