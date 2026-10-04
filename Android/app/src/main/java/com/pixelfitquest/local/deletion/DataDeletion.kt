package com.pixelfitquest.local.deletion

import com.pixelfitquest.feature.healthbonuses.PrefsSessionBonusStore
import com.pixelfitquest.feature.workout.RepEditLog
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationPrefs
import com.pixelfitquest.local.export.LocalExportService

/**
 * What "Delete my data" removes.
 *
 * Workouts, profile, and progression live in Room. Settings and learned range
 * live in [WorkoutOrientationPrefs.PREFS_NAME]. Session bonuses live in
 * [PrefsSessionBonusStore.PREFS_NAME]. The rep-edit log is [RepEditLog.FILE_NAME].
 * Exports sit in [LocalExportService.CACHE_DIR].
 *
 * This build does not write personal workout documents to the cloud
 * ([com.pixelfitquest.local.CloudBackup] is a stub). A signed-in Firebase Auth
 * user can still be deleted with the existing account API. That is the auth
 * record only.
 */
object DataDeletion {

    val preferenceNames: List<String> = listOf(
        WorkoutOrientationPrefs.PREFS_NAME,
        PrefsSessionBonusStore.PREFS_NAME,
    )

    val privateFiles: List<String> = listOf(RepEditLog.FILE_NAME)

    const val EXPORT_CACHE_DIR: String = LocalExportService.CACHE_DIR

    data class Plan(
        val clearDatabase: Boolean,
        val preferenceNames: List<String>,
        val privateFiles: List<String>,
        val exportCacheDir: String,
        val deleteAuthUser: Boolean,
        /** False while the app stores no personal cloud documents. */
        val cloudDocumentsDeletable: Boolean,
    )

    fun plan(signedIn: Boolean): Plan = Plan(
        clearDatabase = true,
        preferenceNames = preferenceNames,
        privateFiles = privateFiles,
        exportCacheDir = EXPORT_CACHE_DIR,
        deleteAuthUser = signedIn,
        cloudDocumentsDeletable = false,
    )

    data class Attempt(
        val signedIn: Boolean,
        val authDeleteSucceeded: Boolean,
    )

    data class Result(
        val localCleared: Boolean,
        val authUserDeleted: Boolean,
        val cloudDocumentsRemoved: Boolean,
        val message: String,
    )

    fun result(attempt: Attempt): Result {
        val authDeleted = attempt.signedIn && attempt.authDeleteSucceeded
        val message = when {
            !attempt.signedIn ->
                "Deleted workouts, logs, settings, and app files on this phone. No account was signed in."
            authDeleted ->
                "Deleted workouts, logs, settings, and app files on this phone, and deleted the signed-in account. Workout history is not stored in the cloud."
            else ->
                "Deleted workouts, logs, settings, and app files on this phone. The signed-in account was not removed. Workout history is not stored in the cloud."
        }
        return Result(
            localCleared = true,
            authUserDeleted = authDeleted,
            cloudDocumentsRemoved = false,
            message = message,
        )
    }
}
