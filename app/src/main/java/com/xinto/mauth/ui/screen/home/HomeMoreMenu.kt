package com.xinto.mauth.ui.screen.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.xinto.mauth.R

enum class HomeMoreMenu(
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    Settings(R.drawable.ic_settings, R.string.settings_title),
    Export(R.drawable.ic_export, R.string.export_title),
    About(R.drawable.ic_info, R.string.about_title)
}