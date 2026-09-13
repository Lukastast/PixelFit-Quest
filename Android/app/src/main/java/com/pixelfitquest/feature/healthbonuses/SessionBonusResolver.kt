package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonusResolution
import java.time.LocalDate
import java.time.ZoneId

/**
 * Once-per-day extras, frozen per workout after the first grant.
 * Local store only — finishing a session never waits on the network.
 */
class SessionBonusResolver(
    private val store: SessionBonusStore,
    private val today: () -> String = {
        LocalDate.now(ZoneId.systemDefault()).toString()
    },
) {
    fun resolve(workoutId: String, snapshot: HealthSnapshot): SessionBonusResolution {
        if (workoutId.isBlank()) {
            return SessionBonusResolution(
                workoutId = workoutId,
                snapshot = snapshot,
                bonuses = emptyList(),
                isNewAward = false,
            )
        }
        val existing = store.loadAward(workoutId)
        if (existing != null) {
            return SessionBonusResolution(
                workoutId = workoutId,
                snapshot = snapshot,
                bonuses = existing,
                isNewAward = false,
            )
        }
        val bonuses = SessionBonusEvaluator.evaluate(snapshot, store.claimedKinds(today()))
        if (bonuses.isNotEmpty()) {
            store.saveAward(workoutId, bonuses)
            store.claimKinds(today(), bonuses.map { it.kind })
        }
        return SessionBonusResolution(
            workoutId = workoutId,
            snapshot = snapshot,
            bonuses = bonuses,
            isNewAward = bonuses.isNotEmpty(),
        )
    }
}
