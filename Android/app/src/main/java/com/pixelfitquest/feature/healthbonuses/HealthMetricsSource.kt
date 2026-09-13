package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot

/**
 * On-device health metrics for session bonuses.
 *
 * Implementations must not block starting or finishing a workout, must not
 * prompt for login, and must treat missing metrics as "no bonus" rather than
 * an error.
 */
interface HealthMetricsSource {
    suspend fun readSnapshot(): HealthSnapshot
}
