package com.pixelfitquest.feature.healthbonuses

import android.util.Log
import com.pixelfitquest.feature.healthbonuses.model.HealthDataOrigin
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthRepository

/**
 * Maps [HealthRepository.readTodayMetrics] into a [HealthSnapshot] for
 * session bonuses. Uses only metrics Health Connect already exposes
 * (steps / step goal today). Sleep, energy, distance, and running stay
 * unset — do not widen Health Connect permissions for this feature.
 */
class HealthConnectMetricsSource(
    private val healthRepository: HealthRepository,
) : HealthMetricsSource {

    override suspend fun readSnapshot(): HealthSnapshot {
        return try {
            if (healthRepository.availability() != HealthConnectStatus.AVAILABLE) {
                return HealthSnapshot.EMPTY
            }
            val metrics = healthRepository.readTodayMetrics()
            val snapshot = HealthSnapshot(
                steps = metrics.steps,
                stepGoal = metrics.stepGoal,
                sleepMinutes = null,
                sleepScore = null,
                energyScore = null,
                distanceMeters = null,
                hadRunningSession = false,
                origin = HealthDataOrigin.HEALTH_CONNECT,
            )
            if (snapshot.hasAnyMetric()) snapshot else snapshot.copy(origin = HealthDataOrigin.NONE)
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect snapshot failed", e)
            HealthSnapshot.EMPTY
        }
    }

    private companion object {
        const val TAG = "HealthConnectMetrics"
    }
}
