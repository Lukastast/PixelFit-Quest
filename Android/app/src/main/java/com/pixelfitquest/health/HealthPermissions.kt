package com.pixelfitquest.health

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord

object HealthPermissions {
    fun required(): Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    )

    fun hasStepsRead(granted: Set<String>): Boolean =
        HealthPermission.getReadPermission(StepsRecord::class) in granted
}
