package com.xinto.mauth.ui.screen.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.xinto.mauth.R

enum class HomeMoreMenu(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int
) {
    Groups(R.drawable.ic_label, R.string.home_more_groups),
    Export(R.drawable.ic_mobile_arrow_right, R.string.export_title),
    Import(R.drawable.ic_mobile_arrow_down, R.string.home_more_import),
    Settings(R.drawable.ic_settings, R.string.settings_title),
    About(R.drawable.ic_info_filled, R.string.about_title);

    companion object {

        val groups = listOf(
            listOf(Groups),
            listOf(Export, Import),
            listOf(Settings, About)
        )
    }
}
