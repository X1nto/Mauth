package com.xinto.mauth.screenshot

import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.LosslessWebPImageIoFormat
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@OptIn(ExperimentalRoborazziApi::class)
class PreviewScreenshotTester : ComposePreviewTester<AndroidPreviewJUnit4TestParameter> by AndroidComposePreviewTester(
    capturer = { parameter ->
        parameter.preview.captureRoboImage(
            filePath = parameter.preview.screenshotPath(),
            // Lossless, so verification still compares exact pixels
            roborazziOptions = parameter.roborazziOptions.copy(
                recordOptions = parameter.roborazziOptions.recordOptions.copy(
                    imageIoFormat = LosslessWebPImageIoFormat(),
                ),
            ),
            roborazziComposeOptions = parameter.roborazziComposeOptions,
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
