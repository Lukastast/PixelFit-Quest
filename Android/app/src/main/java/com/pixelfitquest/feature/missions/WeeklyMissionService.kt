package com.pixelfitquest.feature.missions

import android.content.SharedPreferences
import android.util.Log
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.health.HealthTime
import com.pixelfitquest.local.LocalPixelFitStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One weekly board for home and the quest center. Claims are written only after
 * the XP and coin grant returns, and a mutex keeps two screens from paying twice.
 */
@Singleton
class WeeklyMissionService @Inject constructor(
    private val prefs: SharedPreferences,
    private val localStore: LocalPixelFitStore,
    private val localXpPort: LocalXpPort,
) {
    private val mutex = Mutex()
    private val claimsByWeek = mutableMapOf<String, Set<String>>()

    private val _board = MutableStateFlow(initialBoard())
    val board: StateFlow<WeeklyMissionBoard> = _board.asStateFlow()

    suspend fun sync(workouts: List<Workout>, weeklySteps: Long) {
        mutex.withLock {
            try {
                val weekKey = HealthTime.currentWeekIso()
                val (weekStart, weekEnd) = HealthTime.weekRange()
                val claimed = readClaims(weekKey)
                val evaluated = WeeklyMissions.evaluate(
                    weekKey = weekKey,
                    workouts = workouts,
                    weeklySteps = weeklySteps,
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    claimedIds = claimed,
                )
                val fresh = WeeklyMissions.newlyClaimable(evaluated.missions)
                var claimedAfter = claimed
                if (fresh.isNotEmpty()) {
                    try {
                        grant(fresh)
                        claimedAfter = claimed + fresh.map { it.id }
                        writeClaims(weekKey, claimedAfter)
                    } catch (e: Exception) {
                        Log.e(TAG, "Weekly mission reward failed", e)
                    }
                }
                _board.value = evaluated.copy(
                    missions = evaluated.missions.map { progress ->
                        progress.copy(claimed = progress.definition.id in claimedAfter)
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Weekly mission sync failed", e)
            }
        }
    }

    private suspend fun grant(missions: List<MissionDefinition>) {
        val xp = missions.sumOf { it.xp.coerceAtLeast(0) }
        val coins = missions.sumOf { it.coins.coerceAtLeast(0) }
        if (xp > 0) {
            localXpPort.awardXp(xp, "weekly_mission")
        }
        if (coins > 0) {
            val current = (localStore.getUserField("coins") as? Number)?.toInt() ?: 0
            localStore.updateUserData(mapOf("coins" to current + coins))
        }
    }

    private fun readClaims(weekKey: String): Set<String> {
        claimsByWeek[weekKey]?.let { return it }
        val stored = prefs.getString(claimKey(weekKey), "")
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        claimsByWeek[weekKey] = stored
        return stored
    }

    private fun writeClaims(weekKey: String, ids: Set<String>) {
        claimsByWeek[weekKey] = ids
        prefs.edit().putString(claimKey(weekKey), ids.sorted().joinToString(",")).apply()
    }

    private fun claimKey(weekKey: String) = "weekly_mission_claims_$weekKey"

    private companion object {
        const val TAG = "WeeklyMissions"

        fun initialBoard(): WeeklyMissionBoard {
            val weekKey = runCatching { HealthTime.currentWeekIso() }.getOrDefault("")
            return WeeklyMissionBoard(
                weekKey = weekKey,
                missions = WeeklyMissions.boardForWeek(weekKey).map { definition ->
                    MissionProgress(definition = definition, current = 0L, claimed = false)
                },
            )
        }
    }
}
