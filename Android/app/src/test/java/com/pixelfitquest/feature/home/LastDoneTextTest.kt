package com.pixelfitquest.feature.home

import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import com.pixelfitquest.feature.home.model.LastDone
import com.pixelfitquest.feature.home.model.LastDoneDay
import com.pixelfitquest.testing.ScreenshotTestBase
import org.junit.Test
import org.robolectric.annotation.Config
import java.time.DayOfWeek

@Config(qualifiers = ScreenshotTestBase.QUALIFIER_PORTRAIT)
class LastDoneTextTest : ScreenshotTestBase() {

    private fun assertText(lastDone: LastDone, expected: String) {
        composeTestRule.setContent { Text(lastDoneText(lastDone)) }
        composeTestRule.onNodeWithText(expected).assertExists()
    }

    @Test fun none() = assertText(LastDone.None, "No recent workouts")
    @Test fun today() = assertText(LastDone.Done(LastDoneDay.Today, 52), "Last done Today, 52 min")
    @Test fun yesterday() = assertText(LastDone.Done(LastDoneDay.Yesterday, 30), "Last done Yesterday, 30 min")
    @Test fun weekday() = assertText(LastDone.Done(LastDoneDay.Weekday(DayOfWeek.TUESDAY), 48), "Last done Tue, 48 min")
    @Test fun daysAgo() = assertText(LastDone.Done(LastDoneDay.DaysAgo(10), 40), "Last done 10 days ago, 40 min")
}
