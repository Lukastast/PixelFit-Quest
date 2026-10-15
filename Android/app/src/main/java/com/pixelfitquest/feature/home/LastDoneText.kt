package com.pixelfitquest.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.LastDone
import com.pixelfitquest.feature.home.model.LastDoneDay
import java.time.format.TextStyle
import java.util.Locale

/** Localized "Last done …" subtitle for the Home next-up panel. */
@Composable
@ReadOnlyComposable
fun lastDoneText(lastDone: LastDone): String = when (lastDone) {
    LastDone.None -> stringResource(R.string.home_no_recent_workouts)
    is LastDone.Done -> {
        val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
        val day = when (val d = lastDone.day) {
            LastDoneDay.Today -> stringResource(R.string.home_last_done_today)
            LastDoneDay.Yesterday -> stringResource(R.string.home_last_done_yesterday)
            is LastDoneDay.Weekday -> d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
            is LastDoneDay.DaysAgo -> pluralStringResource(
                R.plurals.home_last_done_days_ago, d.days.toInt(), d.days.toInt()
            )
        }
        stringResource(R.string.home_last_done_format, day, lastDone.durationMinutes.toInt())
    }
}
