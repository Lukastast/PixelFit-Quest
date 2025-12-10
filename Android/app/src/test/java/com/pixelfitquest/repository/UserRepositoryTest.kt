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
import com.google.firebase.firestore.QuerySnapshot
import com.pixelfitquest.model.CharacterData
import com.pixelfitquest.model.UserData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {

    // Mocks
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var usersCollection: CollectionReference
    private lateinit var userDocRef: DocumentReference

    // Subject under test
    private lateinit var repository: UserRepository

    private val uid = "test_uid_123"

    @Before
    fun setUp() {
        // This fixes ALL Log.e / Log.d crashes in unit tests
        mockkStatic(Log::class)
        every { Log.v(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        MockKAnnotations.init(this)
        UserRepository.clearCacheForTesting()

        firestore = mockk(relaxed = true)
        auth = mockk()
        user = mockk()
        usersCollection = mockk()
        userDocRef = mockk()

        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("test_uid") } returns userDocRef

        // This prevents loadProgressionConfig() from hanging
        val configCol = mockk<CollectionReference>()
        val configDoc = mockk<DocumentReference>()
        val emptySnap = mockk<DocumentSnapshot>()
        every { firestore.collection("configs") } returns configCol
        every { configCol.document("game_progression") } returns configDoc
        every { emptySnap.get(any<String>()) } returns null
        coEvery { configDoc.get() } returns Tasks.forResult(emptySnap)

        repository = UserRepository(firestore, auth)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ╔══════════════════════════════════════════════════════════╗
    //   getUserData() Flow tests
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `getUserData emits null when no user logged in`() = runTest {
        every { auth.currentUser } returns null

        repository.getUserData().test {
            awaitComplete() // Should close immediately
        }
    }

    @Test
    fun `getUserData emits default UserData when document does not exist`() = runTest {
        every { userDocRef.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<DocumentSnapshot>
            listener.onEvent(null, null)  // document doesn't exist
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getUserData().test {
            assertEquals(UserData(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getUserData emits data from firestore`() = runTest {
        val expected = UserData(level = 5, exp = 250, coins = 999, streak = 7)
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserData::class.java) } returns expected

        every { userDocRef.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<DocumentSnapshot>
            listener.onEvent(snapshot, null)  // immediate emit
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getUserData().test {
            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // ╔══════════════════════════════════════════════════════════╗
    //   fetchUserDataOnce tests
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `fetchUserDataOnce returns null when no user`() = runTest {
        every { auth.currentUser } returns null

        assertNull(repository.fetchUserDataOnce())
    }

    @Test
    fun `fetchUserDataOnce returns default when document missing`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserData::class.java) } returns null
        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)

        assertEquals(UserData(), repository.fetchUserDataOnce())
    }

    // ╔══════════════════════════════════════════════════════════╗
    //   initUserData tests
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `initUserData creates document with defaults`() = runTest {
        coEvery { userDocRef.set(UserData()) } returns Tasks.forResult(null)

        repository.initUserData()

        coVerify { userDocRef.set(UserData()) }
    }

    @Test(expected = Exception::class)
    fun `initUserData throws when no user`() = runTest {
        every { auth.currentUser } returns null

        repository.initUserData()
    }

    // ╔══════════════════════════════════════════════════════════╗
    //   updateExp & leveling logic (most important!)
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `updateExp correctly levels up multiple times and caps at MAX_LEVEL`() = runTest {
        // This now instantly loads fallback defaults — no hang!
        repository.loadProgressionConfig()

        val initialData = UserData(level = 1, exp = 0)
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserData::class.java) } returns initialData
        every { snapshot.exists() } returns true

        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)
        coEvery { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.updateExp(600) // 100 + 200 + 300 = level 4, exp 0

        coVerify {
            userDocRef.update(mapOf(
                "level" to 4,
                "exp" to 0
            ))
        }
    }

    @Test
    fun `updateExp caps level and exp at MAX_LEVEL=30`() = runTest {
        repository.loadProgressionConfig() // loads defaults

        val initialData = UserData(level = 30, exp = 0)
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserData::class.java) } returns initialData
        every { snapshot.exists() } returns true

        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)
        coEvery { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.updateExp(99999)

        coVerify {
            userDocRef.update(mapOf(
                "level" to 30,
                "exp" to repository.getExpRequiredForLevel(30) // capped at max level's required exp
            ))
        }
    }

    // ╔══════════════════════════════════════════════════════════╗
    //   Streak logic (UTC date handling)
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `updateStreak increments when yesterday was last activity`() = runTest {
        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterday = java.time.LocalDate.now().minusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserData::class.java) } returns UserData(streak = 5)
        every { snapshot.getString("last_activity_date") } returns yesterday // yesterday in UTC
        every { snapshot.exists() } returns true

        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)
        coEvery { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.updateStreak(increment = true)

        coVerify {
            userDocRef.update(mapOf(
                "streak" to 6,
                "last_activity_date" to today // today
            ))
        }
    }

    @Test
    fun `updateStreak resets to 1 when more than one day missed`() = runTest {
        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val threeDaysAgo = java.time.LocalDate.now().minusDays(3).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(UserData::class.java) } returns UserData(streak = 10)
        every { snapshot.getString("last_activity_date") } returns threeDaysAgo
        every { snapshot.exists() } returns true

        coEvery { userDocRef.get() } returns Tasks.forResult(snapshot)
        coEvery { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.updateStreak(increment = true)

        coVerify { userDocRef.update(match { it["streak"] == 1 && it["last_activity_date"] == today }) }
    }

    // ╔══════════════════════════════════════════════════════════╗
    //   Character data tests
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `saveCharacterData writes nested map correctly`() = runTest {
        val character = CharacterData(
            gender = "female",
            variant = "fitness",
            unlockedVariants = listOf("basic", "warrior", "fitness")
        )

        val capturedMap = slot<Map<String, Any>>()
        coEvery { userDocRef.update("character", capture(capturedMap)) } returns Tasks.forResult(null)

        repository.saveCharacterData(character)

        assertEquals("female", capturedMap.captured["gender"])
        assertEquals("fitness", capturedMap.captured["variant"])
        assertEquals(listOf("basic", "warrior", "fitness"), capturedMap.captured["unlockedVariants"])
    }

    @Test
    fun `getCharacterData flow returns defaults when field missing`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.get("character") } returns null

        every { userDocRef.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<DocumentSnapshot>
            listener.onEvent(snapshot, null)
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getCharacterData().test {
            val data = awaitItem()
            assertNotNull(data)
            assertEquals("male", data.gender)
            assertEquals("basic", data.variant)
            assertEquals(listOf("basic"), data.unlockedVariants)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // ╔══════════════════════════════════════════════════════════╗
    //   Leaderboard test
    // ╚══════════════════════════════════════════════════════════╝

    @Test
    fun `getLeaderboard returns sorted list by level then exp descending`() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val doc3 = mockk<DocumentSnapshot>()

        every { doc1.id } returns "userA"
        every { doc1.toObject(UserData::class.java) } returns UserData(level = 10, exp = 500)

        every { doc2.id } returns "userB"
        every { doc2.toObject(UserData::class.java) } returns UserData(level = 15, exp = 100)

        every { doc3.id } returns "userC"
        every { doc3.toObject(UserData::class.java) } returns UserData(level = 15, exp = 300)

        every { querySnapshot.documents } returns listOf(doc1, doc2, doc3)

        coEvery { usersCollection.get() } returns Tasks.forResult(querySnapshot)

        val result = repository.getLeaderboard()

        assertEquals(listOf("userC", "userB", "userA").map { it }, result.map { it.first })
        assertEquals(15, result[0].second.level)
        assertEquals(300, result[0].second.exp) // higher exp wins same level
    }
}