package com.pixelfitquest.feature.healthbonuses

import android.util.Log
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonusResolution
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionBonusService @Inject constructor(
    private val source: HealthMetricsSource,
    private val resolver: SessionBonusResolver,
) {
    suspend fun resolveForWorkout(workoutId: String): SessionBonusResolution {
        val snapshot = try {
            source.readSnapshot()
        } catch (e: Exception) {
            Log.w(TAG, "Health snapshot failed; granting no extras", e)
            HealthSnapshot.EMPTY
        }
        return resolver.resolve(workoutId, snapshot)
    }

    private companion object {
        const val TAG = "SessionBonusService"
    }
}
