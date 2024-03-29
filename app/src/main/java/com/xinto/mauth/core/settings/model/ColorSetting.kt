package com.xinto.mauth.core.settings.model

import android.os.Build

enum class ColorSetting {
    Dynamic,
    MothPurple,
    BlueberryBlue,
    LemonYellow,
    LimeGreen,
    OrangeOrange,
    SkyCyan;

    companion object {
        val DEFAULT = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Dynamic else MothPurple

        val validEntries = entries.filter {
            it != Dynamic || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }
    }
}