package com.xinto.mauth.ui.screen.export.state

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xinto.mauth.domain.account.model.DomainAccount

@Composable
fun ExportScreenSuccess(
    accounts: List<DomainAccount>
) {
    Surface {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(
                items = accounts,
                key = { it.id }
            ) {
                Column {

                }
            }
        }
    }
}