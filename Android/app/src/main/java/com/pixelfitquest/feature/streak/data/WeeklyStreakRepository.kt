package com.pixelfitquest.feature.streak.data

import android.util.Log
import androidx.room.withTransaction
import com.pixelfitquest.feature.streak.model.SessionRecordResult
import com.pixelfitquest.feature.streak.model.StreakClock
import com.pixelfitquest.feature.streak.model.WeeklyStreakEvaluator
import com.pixelfitquest.feature.streak.model.WeeklyStreakSnapshot
import com.pixelfitquest.feature.streak.model.WeeklyStreakState
import com.pixelfitquest.local.LocalPixelFitStore
import com.pixelfitquest.local.UserProgression
import com.pixelfitquest.local.db.PixelFitDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyStreakRepository @Inject constructor(
    private val database: PixelFitDatabase,
    private val dao: WeeklyStreakDao,
    private val clock: StreakClock,
    private val localStore: LocalPixelFitStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSnapshot(): Flow<WeeklyStreakSnapshot> {
        return dao.observeState()
            .onStart { emit(null) }
            .flatMapLatest { entity ->
                val weekStart = currentWeekStart()
                dao.observeSessionCountInWeek(weekStart.toString()).map { count ->
                    val state = (entity ?: WeeklyStreakStateEntity()).toModel()
                    WeeklyStreakEvaluator.toSnapshot(state, count, weekStart)
                }
            }
    }

    suspend fun snapshot(): WeeklyStreakSnapshot {
        val weekStart = currentWeekStart()
        val state = ensureState()
        val count = dao.countSessionsInWeek(weekStart.toString())
        return WeeklyStreakEvaluator.toSnapshot(state, count, weekStart)
    }

    suspend fun reconcile(): WeeklyStreakSnapshot {
        return database.withTransaction {
            val weekStart = currentWeekStart()
            persistApply(ensureState(), dao.countSessionsInWeek(weekStart.toString()), weekStart)
        }
    }

    suspend fun setTargetSessionsPerWeek(target: Int): WeeklyStreakSnapshot {
        return database.withTransaction {
            val weekStart = currentWeekStart()
            val clamped = WeeklyStreakEvaluator.clampTarget(target)
            val state = ensureState().copy(targetSessionsPerWeek = clamped)
            persistApply(state, dao.countSessionsInWeek(weekStart.toString()), weekStart)
        }
    }

    suspend fun recordCompletedSession(
        workoutId: String,
        atMillis: Long = clock.nowMillis(),
    ): SessionRecordResult {
        if (workoutId.isBlank()) {
            return SessionRecordResult(
                snapshot = snapshot(),
                weekJustCompleted = false,
                xpAwarded = 0,
                newlyUnlockedSkins = emptyList(),
                duplicateSession = false,
            )
        }
        val result = database.withTransaction {
            val sessionWeek = WeeklyStreakEvaluator.weekStartDate(atMillis, clock.zone())
            val inserted = dao.insertSession(
                WeeklySessionEntity(
                    workoutId = workoutId,
                    completedAtEpochMillis = atMillis,
                    weekStartIso = sessionWeek.toString(),
                )
            )
            val duplicate = inserted == -1L
            val count = dao.countSessionsInWeek(sessionWeek.toString())
            val mutation = WeeklyStreakEvaluator.apply(ensureState(), count, sessionWeek)
            dao.upsertState(mutation.state.toEntity())
            val snapshot = WeeklyStreakEvaluator.toSnapshot(mutation.state, count, sessionWeek)
            SessionRecordResult(
                snapshot = snapshot,
                weekJustCompleted = mutation.weekJustCompleted && !duplicate,
                xpAwarded = if (duplicate) 0 else mutation.xpAwarded,
                newlyUnlockedSkins = if (duplicate) emptyList() else mutation.newlyUnlockedSkins,
                duplicateSession = duplicate,
            )
        }
        if (!result.duplicateSession) {
            alignProfileDailyStreak(atMillis)
        }
        return result
    }

    suspend fun forgetSession(workoutId: String) {
        if (workoutId.isBlank()) return
        dao.deleteSession(workoutId)
    }

    /**
     * Keep [UserProfileEntity.streak] / lastActivityDate / lastStreakUpdateDate coherent
     * with workout completion via [UserProgression.applyStreak] (daily continuity).
     * Weekly goal state stays in WeeklyStreakStateEntity on the same pixelfit.db.
     */
    private suspend fun alignProfileDailyStreak(atMillis: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date(atMillis))
        val yesterday = dateFormat.format(Date(atMillis - MILLIS_PER_DAY))
        localStore.replaceProfile { current ->
            if (current.lastStreakUpdateDate == today) {
                return@replaceProfile current
            }
            val result = UserProgression.applyStreak(
                currentStreak = current.streak,
                lastActivityDate = current.lastActivityDate,
                today = today,
                yesterday = yesterday,
                increment = true,
                reset = false,
            )
            current.copy(
                streak = result.streak,
                lastActivityDate = result.lastActivityDate,
                lastStreakUpdateDate = today,
            )
        }
    }

    suspend fun peekPendingXp(): Int = ensureState().pendingXp

    suspend fun withConsumedPendingXp(grant: suspend (Int) -> Unit) {
        val amount = database.withTransaction {
            val state = ensureState()
            if (state.pendingXp <= 0) return@withTransaction 0
            dao.upsertState(state.copy(pendingXp = 0).toEntity())
            state.pendingXp
        }
        if (amount <= 0) return
        try {
            grant(amount)
        } catch (t: Throwable) {
            database.withTransaction {
                val state = ensureState()
                dao.upsertState(state.copy(pendingXp = state.pendingXp + amount).toEntity())
            }
            throw t
        }
    }

    private suspend fun persistApply(
        state: WeeklyStreakState,
        sessionsThisWeek: Int,
        weekStart: LocalDate,
    ): WeeklyStreakSnapshot {
        val mutation = WeeklyStreakEvaluator.apply(state, sessionsThisWeek, weekStart)
        dao.upsertState(mutation.state.toEntity())
        return WeeklyStreakEvaluator.toSnapshot(mutation.state, sessionsThisWeek, weekStart)
    }

    private suspend fun ensureState(): WeeklyStreakState {
        val existing = dao.getState()
        if (existing != null) return existing.toModel()
        val fresh = WeeklyStreakStateEntity()
        dao.upsertState(fresh)
        return fresh.toModel()
    }

    private fun currentWeekStart(): LocalDate =
        WeeklyStreakEvaluator.weekStartDate(clock.nowMillis(), clock.zone())

    companion object {
        private const val TAG = "WeeklyStreakRepo"
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

        fun logFailure(message: String, error: Throwable) {
            Log.w(TAG, message, error)
        }
    }
}
