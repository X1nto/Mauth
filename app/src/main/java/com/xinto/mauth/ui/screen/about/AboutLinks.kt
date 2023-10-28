package com.xinto.mauth.ui.screen.about

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.xinto.mauth.R

@Immutable
data class AboutLink(
    @DrawableRes
    val icon: Int,

    @StringRes
    val title: Int,

    val url: String
) {
    companion object {
        val defaultLinks = setOf(
            AboutLink(
                icon = R.drawable.ic_github,
                title = R.string.about_links_source,
                url = "https://github.com/X1nto/Mauth"
            ),
            AboutLink(
                icon = R.drawable.ic_bug,
                title = R.string.about_links_feedback,
                url = "https://github.com/X1nto/Mauth/issues"
            ),
        )
    }
}
