package com.pixelfitquest.health

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord

object HealthPermissions {
    fun required(): Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    )

    fun hasStepsRead(granted: Set<String>): Boolean =
        HealthPermission.getReadPermission(StepsRecord::class) in granted

    fun hasSleepRead(granted: Set<String>): Boolean =
        HealthPermission.getReadPermission(SleepSessionRecord::class) in granted

    fun hasHeartRateRead(granted: Set<String>): Boolean =
        HealthPermission.getReadPermission(HeartRateRecord::class) in granted

    fun hasRestingHeartRateRead(granted: Set<String>): Boolean =
        HealthPermission.getReadPermission(RestingHeartRateRecord::class) in granted

    fun hasExerciseRead(granted: Set<String>): Boolean =
        HealthPermission.getReadPermission(ExerciseSessionRecord::class) in granted
}
