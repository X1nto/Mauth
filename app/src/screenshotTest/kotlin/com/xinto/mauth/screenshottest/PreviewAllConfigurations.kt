package com.xinto.mauth.screenshottest

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices.FOLDABLE
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Preview

private const val TABLET_LANDSCAPE = "spec:width=1280dp,height=800dp,dpi=240"
private const val PHONE_LANDSCAPE = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420"

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@Preview(name = "Phone", device = PHONE, showSystemUi = true)
@Preview(name = "Phone Dark", device = PHONE, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Preview(name = "Phone Landscape", device = PHONE_LANDSCAPE, showSystemUi = true)
@Preview(name = "Phone Landscape Dark", device = PHONE_LANDSCAPE, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Preview(name = "Tablet Landscape", device = TABLET_LANDSCAPE, showSystemUi = true)
@Preview(name = "Tablet Landscape Dark", device = TABLET_LANDSCAPE, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Preview(name = "Foldable", device = FOLDABLE, showSystemUi = true)
@Preview(name = "Foldable Dark", device = FOLDABLE, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
annotation class PreviewAllConfigurations
