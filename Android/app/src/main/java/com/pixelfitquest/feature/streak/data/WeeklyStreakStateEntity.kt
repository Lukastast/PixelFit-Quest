package com.pixelfitquest.feature.streak.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixelfitquest.feature.streak.model.DEFAULT_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.WeeklyStreakState
import java.time.LocalDate

@Entity(tableName = "weekly_streak_state")
data class WeeklyStreakStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val targetSessionsPerWeek: Int = DEFAULT_WEEKLY_TARGET,
    val currentStreakWeeks: Int = 0,
    val longestStreakWeeks: Int = 0,
    val lastCompletedWeekStart: String = "",
    val lastXpAwardWeekStart: String = "",
    val lastXpAwardAmount: Int = 0,
    val pendingXp: Int = 0,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}

fun WeeklyStreakStateEntity.toModel(): WeeklyStreakState = WeeklyStreakState(
    targetSessionsPerWeek = targetSessionsPerWeek,
    currentStreakWeeks = currentStreakWeeks,
    longestStreakWeeks = longestStreakWeeks,
    lastCompletedWeekStart = lastCompletedWeekStart.toLocalDateOrNull(),
    lastXpAwardWeekStart = lastXpAwardWeekStart.toLocalDateOrNull(),
    lastXpAwardAmount = lastXpAwardAmount,
    pendingXp = pendingXp,
)

fun WeeklyStreakState.toEntity(): WeeklyStreakStateEntity = WeeklyStreakStateEntity(
    id = WeeklyStreakStateEntity.SINGLETON_ID,
    targetSessionsPerWeek = targetSessionsPerWeek,
    currentStreakWeeks = currentStreakWeeks,
    longestStreakWeeks = longestStreakWeeks,
    lastCompletedWeekStart = lastCompletedWeekStart?.toString().orEmpty(),
    lastXpAwardWeekStart = lastXpAwardWeekStart?.toString().orEmpty(),
    lastXpAwardAmount = lastXpAwardAmount,
    pendingXp = pendingXp,
)

private fun String.toLocalDateOrNull(): LocalDate? =
    if (isBlank()) null else runCatching { LocalDate.parse(this) }.getOrNull()
