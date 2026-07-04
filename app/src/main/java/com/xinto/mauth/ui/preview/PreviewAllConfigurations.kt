package com.xinto.mauth.ui.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices.FOLDABLE
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Phone - Portrait/Light",
    device = PHONE,
    locale = "en",
    showSystemUi = true
)
@Preview(
    name = "Phone - Landscape/Light",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    locale = "en",
    showSystemUi = true
)
@Preview(
    name = "Unfolded Foldable/Light",
    device = FOLDABLE,
    locale = "en",
    showSystemUi = true
)
@Preview(
    name = "Tablet - Portrait/Light",
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
    locale = "en",
    showSystemUi = true
)
@Preview(
    name = "Tablet - Landscape/Light",
    device = TABLET,
    locale = "en",
    showSystemUi = true
)
@Preview(
    name = "Phone - Portrait/Dark",
    device = PHONE,
    locale = "en",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = true
)
@Preview(
    name = "Phone - Landscape/Dark",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    locale = "en",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = true
)
@Preview(
    name = "Unfolded Foldable/Dark",
    device = FOLDABLE,
    locale = "en",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = true
)
@Preview(
    name = "Tablet - Portrait/Dark",
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
    locale = "en",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = true
)
@Preview(
    name = "Tablet - Landscape/Dark",
    device = TABLET,
    locale = "en",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = true
)
annotation class PreviewAllConfigurations
