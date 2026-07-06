package com.xinto.mauth.screenshot

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import com.xinto.mauth.ui.theme.MauthTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

abstract class RoborazziStoreScreenshotTest(
    private val spec: StoreScreenshot,
    private val outputSubdir: String,
) {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun capture() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MauthTheme(theme = ThemeSetting.Dark, color = ColorSetting.BlueberryBlue) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    StoreScreenshotContent(spec.id)
                }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()

        composeRule.onRoot()
            .captureRoboImage("../fastlane/metadata/android/en-US/images/$outputSubdir/${spec.fileName}")
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-420dpi", application = android.app.Application::class)
class PhoneStoreScreenshotTest(spec: StoreScreenshot) : RoborazziStoreScreenshotTest(spec, "phoneScreenshots") {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<StoreScreenshot> = storeScreenshots
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w600dp-h960dp-320dpi", application = android.app.Application::class)
class SevenInchStoreScreenshotTest(spec: StoreScreenshot) : RoborazziStoreScreenshotTest(spec, "sevenInchScreenshots") {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<StoreScreenshot> = storeScreenshots
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1280dp-h800dp-320dpi", application = android.app.Application::class)
class TenInchStoreScreenshotTest(spec: StoreScreenshot) : RoborazziStoreScreenshotTest(spec, "tenInchScreenshots") {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<StoreScreenshot> = storeScreenshots
    }
}

