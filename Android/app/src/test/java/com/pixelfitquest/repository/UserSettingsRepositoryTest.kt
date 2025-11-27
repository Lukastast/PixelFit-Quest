package com.pixelfitquest.repository

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.pixelfitquest.model.UserSettings
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserSettingsRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var usersCollection: CollectionReference
    private lateinit var userDocRef: DocumentReference
    private lateinit var repository: UserSettingsRepository

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.v(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        MockKAnnotations.init(this)

        firestore = mockk(relaxed = true)
        auth = mockk()
        user = mockk()
        usersCollection = mockk()
        userDocRef = mockk()

        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("test_uid") } returns userDocRef

        repository = UserSettingsRepository(firestore, auth)
    }

    @Test
    fun `getUserSettings emits default when document missing`() = runTest {
        every { userDocRef.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<DocumentSnapshot>
            listener.onEvent(null, null)
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getUserSettings().test {
            assertEquals(UserSettings(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getUserSettings emits data from firestore`() = runTest {
        val expected = UserSettings(height = 180)
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserSettings::class.java) } returns expected

        every { userDocRef.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<DocumentSnapshot>
            listener.onEvent(snapshot, null)
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getUserSettings().test {
            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateUserSettings creates document with defaults + updates if not exists`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.exists() } returns false

        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)
        coEvery { userDocRef.set(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.updateUserSettings(mapOf("height" to 175, "weightGoal" to 80))

        coVerify {
            userDocRef.set(match<Map<String, Any>> {
                it["height"] == 175 && it["weightGoal"] == 80
                // default values are included automatically via UserSettings()
            })
        }
    }

    @Test
    fun `updateUserSettings updates existing document normally`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.exists() } returns true

        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)
        coEvery { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.updateUserSettings(mapOf("height" to 185))

        coVerify { userDocRef.update(mapOf("height" to 185)) }
    }

    @Test
    fun `getUserSettings completes immediately when no user logged in`() = runTest {
        every { auth.currentUser } returns null
        repository = UserSettingsRepository(firestore, auth)

        repository.getUserSettings().test {
            awaitComplete() // Should close immediately without emitting
        }
    }

    @Test
    fun `updateUserSettings throws when no user logged in`() = runTest {
        every { auth.currentUser } returns null
        repository = UserSettingsRepository(firestore, auth)

        assertFailsWith<IllegalStateException> {
            repository.updateUserSettings(mapOf("height" to 180))
        }
    }
}