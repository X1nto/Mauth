package com.xinto.mauth.ui.screen.export.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xinto.mauth.domain.account.model.DomainExportAccount
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.screen.export.component.ZxingQrImage

@Composable
fun ExportScreenSuccess(
    accounts: List<DomainExportAccount>,
    onCopyUrlToClipboard: (DomainExportAccount) -> Unit
) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = accounts,
            key = { it.id }
        ) { account ->
            Surface(
                onClick = { onCopyUrlToClipboard(account) },
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                border = CardDefaults.outlinedCardBorder(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        ZxingQrImage(
                            modifier = Modifier.fillMaxSize(),
                            data = account.url,
                            backgroundColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (account.icon != null) {
                                    UriImage(uri = account.icon)
                                } else {
                                    Text(
                                        text = account.shortLabel,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                        Column {
                            if (account.issuer.isNotEmpty()) {
                                Text(
                                    text = account.issuer,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                text = account.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}