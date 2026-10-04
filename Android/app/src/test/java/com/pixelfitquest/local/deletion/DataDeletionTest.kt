package com.pixelfitquest.local.deletion

import com.pixelfitquest.feature.healthbonuses.PrefsSessionBonusStore
import com.pixelfitquest.feature.workout.RepEditLog
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationPrefs
import com.pixelfitquest.firebase.model.User
import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.local.export.LocalExportService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataDeletionTest {

    @Test
    fun planWipesLocalStoresAndDoesNotInventCloudDocuments() {
        val guest = DataDeletion.plan(signedIn = false)
        assertTrue(guest.clearDatabase)
        assertEquals(
            listOf(WorkoutOrientationPrefs.PREFS_NAME, PrefsSessionBonusStore.PREFS_NAME),
            guest.preferenceNames,
        )
        assertEquals("pixelfitquest_prefs", guest.preferenceNames.first())
        assertEquals(listOf(RepEditLog.FILE_NAME), guest.privateFiles)
        assertEquals(LocalExportService.CACHE_DIR, guest.exportCacheDir)
        assertFalse(guest.deleteAuthUser)
        assertFalse(guest.cloudDocumentsDeletable)

        val signedIn = DataDeletion.plan(signedIn = true)
        assertTrue(signedIn.deleteAuthUser)
        assertFalse(signedIn.cloudDocumentsDeletable)
    }

    @Test
    fun resultNeverClaimsCloudWorkoutsOrImuFilesWereRemoved() {
        val guest = DataDeletion.result(DataDeletion.Attempt(signedIn = false, authDeleteSucceeded = false))
        assertTrue(guest.localCleared)
        assertFalse(guest.authUserDeleted)
        assertFalse(guest.cloudDocumentsRemoved)
        assertTrue(guest.message.contains("No account was signed in"))
        assertFalse(guest.message.contains("IMU", ignoreCase = true))
        assertFalse(guest.message.contains("cloud workout", ignoreCase = true))

        val deleted = DataDeletion.result(DataDeletion.Attempt(signedIn = true, authDeleteSucceeded = true))
        assertTrue(deleted.authUserDeleted)
        assertFalse(deleted.cloudDocumentsRemoved)
        assertTrue(deleted.message.contains("not stored in the cloud"))

        val failed = DataDeletion.result(DataDeletion.Attempt(signedIn = true, authDeleteSucceeded = false))
        assertFalse(failed.authUserDeleted)
        assertFalse(failed.cloudDocumentsRemoved)
        assertTrue(failed.message.contains("account was not removed"))
        assertTrue(failed.localCleared)
    }

    @Test
    fun serviceWipesLocalDataEvenWhenAuthDeleteFails() = runBlocking {
        val wipe = RecordingWipe()
        val account = FakeAccount(signedIn = true, deleteFails = true)
        val result = DataDeletionService(wipe, account).deleteMyData()

        assertEquals(listOf("clearDatabase", "clearPreferences", "deletePrivateFiles", "deleteCacheDirectory"), wipe.calls)
        assertEquals(listOf("pixelfitquest_prefs", "health_training_bonuses"), wipe.prefNames)
        assertEquals(listOf("rep_edits.jsonl"), wipe.files)
        assertEquals("exports", wipe.cacheDir)
        assertTrue(account.deleteCalled)
        assertTrue(account.signOutCalled)
        assertFalse(result.authUserDeleted)
        assertFalse(result.cloudDocumentsRemoved)
        assertTrue(result.message.contains("account was not removed"))
    }

    @Test
    fun serviceDoesNotTouchAuthWhenNobodyIsSignedIn() = runBlocking {
        val wipe = RecordingWipe()
        val account = FakeAccount(signedIn = false, deleteFails = false)
        val result = DataDeletionService(wipe, account).deleteMyData()
        assertTrue(wipe.calls.contains("clearDatabase"))
        assertFalse(account.deleteCalled)
        assertFalse(account.signOutCalled)
        assertFalse(result.authUserDeleted)
        assertTrue(result.message.contains("No account was signed in"))
    }

    private class RecordingWipe : LocalDataWipe {
        val calls = mutableListOf<String>()
        var prefNames: List<String> = emptyList()
        var files: List<String> = emptyList()
        var cacheDir: String = ""

        override suspend fun clearDatabase() {
            calls += "clearDatabase"
        }

        override fun clearPreferences(names: List<String>) {
            calls += "clearPreferences"
            prefNames = names
        }

        override fun deletePrivateFiles(names: List<String>) {
            calls += "deletePrivateFiles"
            files = names
        }

        override fun deleteCacheDirectory(name: String) {
            calls += "deleteCacheDirectory"
            cacheDir = name
        }
    }

    private class FakeAccount(
        private var signedIn: Boolean,
        private val deleteFails: Boolean,
    ) : AccountService {
        var deleteCalled = false
        var signOutCalled = false

        override val currentUser: Flow<User?> = emptyFlow()
        override val currentUserId: String get() = if (signedIn) "uid" else ""
        override fun hasUser(): Boolean = signedIn
        override fun getUserProfile(): User = User(id = currentUserId)
        override suspend fun updateDisplayName(newDisplayName: String) = Unit
        override suspend fun linkAccountWithGoogle(idToken: String) = Unit
        override suspend fun linkAccountWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithGoogle(idToken: String) = Unit
        override suspend fun createAccountWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithEmail(email: String, password: String) = Unit
        override suspend fun signOut() {
            signOutCalled = true
            signedIn = false
        }

        override suspend fun deleteAccount() {
            deleteCalled = true
            if (deleteFails) error("auth delete failed")
            signedIn = false
        }
    }
}
