package com.pixelfitquest.testing

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ScreenshotTestBase.DEFAULT_ROBO_SDK])
abstract class ScreenshotTestBase {

    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        const val QUALIFIER_LANDSCAPE = "w891dp-h411dp-land"
        const val QUALIFIER_PORTRAIT = "w411dp-h891dp"
        const val DEFAULT_ROBO_SDK = 35
        val DEFAULT_ROBORAZZI_OPTIONS = RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
        )
    }

    protected fun captureScreenshot(
        name: String,
        node: SemanticsNodeInteraction = composeTestRule.onRoot(),
        roborazziOptions: RoborazziOptions = DEFAULT_ROBORAZZI_OPTIONS,
    ) {
        node.captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
            roborazziOptions = roborazziOptions,
        )
    }
}
