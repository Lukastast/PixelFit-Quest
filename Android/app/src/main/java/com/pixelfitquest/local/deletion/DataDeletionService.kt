package com.pixelfitquest.local.deletion

import com.pixelfitquest.firebase.service.AccountService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deletes on-device data, then the Firebase Auth user if one is signed in.
 * Does not invent a cloud workout delete: [DataDeletion.plan] reports that
 * personal cloud documents are not deletable because none are stored.
 */
@Singleton
class DataDeletionService @Inject constructor(
    private val wipe: LocalDataWipe,
    private val accountService: AccountService,
) {
    suspend fun deleteMyData(): DataDeletion.Result {
        val signedIn = accountService.hasUser()
        val plan = DataDeletion.plan(signedIn)
        check(plan.clearDatabase)
        wipe.clearDatabase()
        wipe.clearPreferences(plan.preferenceNames)
        wipe.deletePrivateFiles(plan.privateFiles)
        wipe.deleteCacheDirectory(plan.exportCacheDir)

        var authDeleted = false
        if (plan.deleteAuthUser) {
            authDeleted = runCatching { accountService.deleteAccount() }.isSuccess
            if (!authDeleted) {
                runCatching { accountService.signOut() }
            }
        }
        return DataDeletion.result(
            DataDeletion.Attempt(
                signedIn = signedIn,
                authDeleteSucceeded = authDeleted,
            ),
        )
    }
}
