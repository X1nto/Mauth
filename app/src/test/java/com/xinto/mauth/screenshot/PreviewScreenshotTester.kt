package com.xinto.mauth.screenshot

import android.os.Looper
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.LosslessWebPImageIoFormat
import com.github.takahirom.roborazzi.RoborazziComposeCaptureOption
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@OptIn(ExperimentalRoborazziApi::class)
class PreviewScreenshotTester : ComposePreviewTester<AndroidPreviewJUnit4TestParameter> by AndroidComposePreviewTester(
    capturer = { parameter ->
        parameter.preview.captureRoboImage(
            filePath = parameter.preview.screenshotPath(),
            roborazziOptions = parameter.roborazziOptions.copy(
                recordOptions = parameter.roborazziOptions.recordOptions.copy(
                    imageIoFormat = LosslessWebPImageIoFormat(),
                ),
            ),
            roborazziComposeOptions = parameter.roborazziComposeOptions.builder()
                .addOption(PlatformDialogWidth)
                .build(),
        )
    }
)

// <package>/<File>/<function without _Preview> - <preview name>.webp`
@OptIn(ExperimentalRoborazziApi::class)
private fun ComposablePreview<AndroidPreviewInfo>.screenshotPath(): String {
    val directory = declaringClass.removeSuffix("Kt").replace('.', '/')
    val name = methodName.removeSuffix("_Preview")
    // Preview names like "Phone - Portrait/Light" would otherwise turn into subdirectories
    val previewName = previewInfo.name.replace("/", " - ")
    val fileName = if (previewName.isEmpty()) name else "$name - $previewName"
    return "${roborazziSystemPropertyOutputDirectory()}/$directory/$fileName.webp"
}

@OptIn(ExperimentalRoborazziApi::class)
private object PlatformDialogWidth : RoborazziComposeCaptureOption {
    override fun beforeCapture() {
        ShadowDialog.getShownDialogs().forEach { dialog ->
            val window = dialog.window ?: return@forEach
            if (window.attributes.width != WRAP_CONTENT)
                return@forEach

            val resources = dialog.context.resources
            val id = resources.getIdentifier("config_prefDialogWidth", "dimen", "android")
            val width = resources.getDimensionPixelSize(id)
            window.setLayout(minOf(width, resources.displayMetrics.widthPixels), WRAP_CONTENT)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    override fun afterCapture() {}
}
