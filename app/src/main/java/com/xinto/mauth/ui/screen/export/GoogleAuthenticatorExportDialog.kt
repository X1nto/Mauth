package com.xinto.mauth.ui.screen.export

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.AlertDialog
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.screen.export.component.ExportQrCode
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID
import kotlin.math.floor

@Composable
fun GoogleAuthenticatorExportDialog(
    modifier: Modifier = Modifier,
    accounts: List<UUID>,
    onDismissRequest: () -> Unit
) {
    val viewModel: GoogleAuthenticatorExportViewModel = koinViewModel {
        parametersOf(accounts)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GoogleAuthenticatorExportDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        state = state,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GoogleAuthenticatorExportDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    state: GoogleAuthenticatorExportState
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        when (state) {
            is GoogleAuthenticatorExportState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is GoogleAuthenticatorExportState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.export_state_empty),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            is GoogleAuthenticatorExportState.Success -> {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val pagerState = rememberPagerState { state.uris.size }
                    HorizontalPager(
                        state = pagerState,
                        flingBehavior = PagerDefaults.flingBehavior(
                            state = pagerState,
                            pagerSnapDistance = PagerSnapDistance.atMost(1),
                        )
                    ) { page ->
                        ExportQrCode(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            data = state.uris[page]
                        )
                    }
                    if (state.uris.size > 1) {
                        PageIndicators(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            pagerState = pagerState
                        )
                    }
                }
            }
            is GoogleAuthenticatorExportState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

private val IndicatorRadius = 4.dp
private val IndicatorSpacing = 6.dp
private val IndicatorPull = 2.dp

@Composable
private fun PageIndicators(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier.height(24.dp)) {
        val spacing = IndicatorSpacing.toPx()
        val radius = IndicatorRadius.toPx()

        val pitch = radius * 2 + spacing
        val startX = (size.width - (pitch * pagerState.pageCount - spacing)) / 2 + radius
        val centerY = size.height / 2

        val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val page = floor(position)
        val fraction = position - page
        val head = page + FastOutSlowInEasing.transform((fraction * 2).coerceAtMost(1f))
        val tail = page + FastOutSlowInEasing.transform((fraction * 2 - 1).coerceAtLeast(0f))

        val from = page.toInt()
        val pull = IndicatorPull.toPx() * 4 * fraction * (1 - fraction)

        repeat(pagerState.pageCount) {
            val shift = when (it) {
                from -> pull
                from + 1 -> -pull
                else -> 0f
            }
            drawCircle(
                color = inactiveColor,
                radius = radius,
                center = Offset(startX + pitch * it + shift, centerY)
            )
        }

        drawRoundRect(
            color = activeColor,
            topLeft = Offset(startX + pitch * tail - radius, centerY - radius),
            size = Size(pitch * (head - tail) + radius * 2, radius * 2),
            cornerRadius = CornerRadius(radius)
        )
    }
}

private val PreviewURIs = List(3) {
    "otpauth://totp/account${it}?secret=secret&issuer=issuer&algorithm=sha1&digits=6&period=30".repeat(3 - it)
}

@PreviewAllConfigurations
@Composable
private fun GoogleAuthenticatorExportDialog_SinglePage_Preview() {
    MauthTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GoogleAuthenticatorExportDialog(
                onDismissRequest = {},
                state = GoogleAuthenticatorExportState.Success(uris = PreviewURIs.take(1))
            )
        }
    }
}

@PreviewAllConfigurations
@Composable
private fun GoogleAuthenticatorExportDialog_MultiPage_Preview() {
    MauthTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GoogleAuthenticatorExportDialog(
                onDismissRequest = {},
                state = GoogleAuthenticatorExportState.Success(uris = PreviewURIs)
            )
        }
    }
}