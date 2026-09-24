package com.pixelfitquest.feature.missions

import android.content.SharedPreferences
import android.util.Log
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.feature.progression.RewardBonus
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

enum class RerollResult {
    SWAPPED,
    ALREADY_USED,
    CLAIMED,
    MISSING,
    NO_ALTERNATIVE,
    CANT_AFFORD,
}

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
                val swap = readSwap(weekKey)
                val evaluated = WeeklyMissions.evaluate(
                    weekKey = weekKey,
                    workouts = workouts,
                    weeklySteps = weeklySteps,
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    claimedIds = claimed,
                    swap = swap,
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
        val variant = localStore.getCharacter().variant
        val payout = RewardBonus.withFitnessFlat(
            baseXp = missions.sumOf { it.xp.coerceAtLeast(0) },
            baseCoins = missions.sumOf { it.coins.coerceAtLeast(0) },
            variant = variant,
        )
        if (payout.xp > 0) {
            localXpPort.awardXp(payout.xp, "weekly_mission")
        }
        if (payout.coins > 0) {
            val current = (localStore.getUserField("coins") as? Number)?.toInt() ?: 0
            localStore.updateUserData(mapOf("coins" to current + payout.coins))
        }
    }

    suspend fun reroll(missionId: String): RerollResult = mutex.withLock {
        val weekKey = HealthTime.currentWeekIso()
        if (readSwap(weekKey) != null) return@withLock RerollResult.ALREADY_USED
        val current = _board.value
        if (current.weekKey != weekKey) return@withLock RerollResult.MISSING
        val mission = current.missions.find { it.definition.id == missionId }
            ?: return@withLock RerollResult.MISSING
        if (mission.claimed || mission.isComplete) return@withLock RerollResult.CLAIMED
        val replacement = WeeklyMissions.replacementFor(
            current.missions.map { it.definition },
            missionId,
        ) ?: return@withLock RerollResult.NO_ALTERNATIVE
        val coins = (localStore.getUserField("coins") as? Number)?.toInt() ?: 0
        if (coins < WeeklyMissions.REROLL_COST) return@withLock RerollResult.CANT_AFFORD
        localStore.updateUserData(mapOf("coins" to coins - WeeklyMissions.REROLL_COST))
        val swap = MissionSwap(fromId = missionId, toId = replacement.id)
        writeSwap(weekKey, swap)
        val claimed = readClaims(weekKey)
        var swapped = mission.copy(
            definition = replacement,
            claimed = replacement.id in claimed,
        )
        if (swapped.isComplete && !swapped.claimed) {
            grant(listOf(replacement))
            val claimedAfter = claimed + replacement.id
            writeClaims(weekKey, claimedAfter)
            swapped = swapped.copy(claimed = true)
        }
        _board.value = current.copy(
            rerollAvailable = false,
            missions = current.missions.map { progress ->
                if (progress.definition.id == missionId) swapped else progress
            },
        )
        RerollResult.SWAPPED
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

    private fun readSwap(weekKey: String): MissionSwap? {
        val stored = prefs.getString(swapKey(weekKey), "").orEmpty()
        val parts = stored.split('|')
        if (parts.size != 2) return null
        val fromId = parts[0].trim()
        val toId = parts[1].trim()
        if (fromId.isEmpty() || toId.isEmpty()) return null
        return MissionSwap(fromId, toId)
    }

    private fun writeSwap(weekKey: String, swap: MissionSwap) {
        prefs.edit().putString(swapKey(weekKey), "${swap.fromId}|${swap.toId}").apply()
    }

    private fun swapKey(weekKey: String) = "weekly_mission_swap_$weekKey"

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
