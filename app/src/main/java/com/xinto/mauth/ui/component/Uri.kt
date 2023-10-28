package com.xinto.mauth.ui.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberUriHandler(): UriHandler {
    val context = LocalContext.current
    return remember(context) {
        UriHandler(context)
    }
}

@Immutable
class UriHandler(private val context: Context) {

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

}