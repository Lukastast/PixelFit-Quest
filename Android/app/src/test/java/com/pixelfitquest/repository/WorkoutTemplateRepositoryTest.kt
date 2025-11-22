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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.pixelfitquest.model.ExerciseType
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.model.WorkoutPlanItem
import com.pixelfitquest.model.WorkoutTemplate
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class WorkoutTemplateRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var usersCollection: CollectionReference
    private lateinit var templatesCollection: CollectionReference
    private lateinit var repository: WorkoutTemplateRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.d(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        firestore = mockk()
        auth = mockk()
        user = mockk()
        usersCollection = mockk()
        templatesCollection = mockk()

        every { auth.currentUser } returns user
        every { user.uid } returns "testUserId"
        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("testUserId") } returns mockk {
            every { collection("templates") } returns templatesCollection
        }

        repository = WorkoutTemplateRepository(firestore, auth)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `saveTemplate saves template successfully`() = runTest {
        val template = WorkoutTemplate(
            id = "t1",
            name = "Push Day Template",
            plan = WorkoutPlan(
                items = listOf(
                    WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 3, weight = 100f),
                    WorkoutPlanItem(ExerciseType.BICEP_CURL, sets = 3, weight = 60f)
                )
            ),
            createdAt = "2025-11-22"
        )

        val docRef = mockk<DocumentReference>()
        every { templatesCollection.document("t1") } returns docRef
        coEvery { docRef.set(any()) } returns Tasks.forResult(null)

        repository.saveTemplate(template)

        coVerify { docRef.set(template.toMap()) }
    }

    @Test
    fun `saveTemplate throws when no user logged in`() = runTest {
        every { auth.currentUser } returns null
        repository = WorkoutTemplateRepository(firestore, auth)

        val template = WorkoutTemplate(
            id = "t1",
            name = "Test",
            plan = WorkoutPlan(
                items = listOf(
                    WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 3)
                )
            ),
            createdAt = "2025-11-22"
        )

        assertFailsWith<IllegalStateException> {
            repository.saveTemplate(template)
        }
    }

    @Test
    fun `getTemplates flow emits parsed templates`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()

        every { doc1.id } returns "t1"
        every { doc1.data } returns mapOf(
            "id" to "t1",
            "name" to "Push Day",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "tricep-extension",
                    "sets" to 3,
                    "weight" to 60.0
                )
            ),
            "createdAt" to "2025-11-22"
        )

        every { doc2.id } returns "t2"
        every { doc2.data } returns mapOf(
            "id" to "t2",
            "name" to "Pull Day",
            "plan" to listOf(
                mapOf(
                    "exercise" to "seated-rows",
                    "sets" to 4,
                    "weight" to 80.0
                ),
                mapOf(
                    "exercise" to "lat-pulldown",
                    "sets" to 3,
                    "weight" to 70.0
                )
            ),
            "createdAt" to "2025-11-21"
        )

        every { snapshot.documents } returns listOf(doc1, doc2)

        val query = mockk<Query>()
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns query
        every { query.addSnapshotListener(capture(listenerSlot)) } answers {
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getTemplates().test(timeout = 2.seconds) {
            // Trigger the listener after Flow is collected
            listenerSlot.captured.onEvent(snapshot, null)

            val templates = awaitItem()
            assertEquals(2, templates.size)
            assertEquals("Push Day", templates[0].name)
            assertEquals("Pull Day", templates[1].name)
            assertEquals("t1", templates[0].id)
            assertEquals("t2", templates[1].id)
            assertEquals(2, templates[0].plan.items.size)
            assertEquals(ExerciseType.BENCH_PRESS, templates[0].plan.items[0].exercise)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTemplates flow emits empty list on error`() = runTest {
        val query = mockk<Query>()
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns query
        every { query.addSnapshotListener(capture(listenerSlot)) } answers {
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getTemplates().test(timeout = 2.seconds) {
            // Trigger error after Flow is collected
            listenerSlot.captured.onEvent(null, mockk<FirebaseFirestoreException>(relaxed = true))

            val templates = awaitItem()
            assertEquals(0, templates.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTemplates flow filters out unparseable documents`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val goodDoc = mockk<DocumentSnapshot>()
        val badDoc = mockk<DocumentSnapshot>()

        every { goodDoc.id } returns "t1"
        every { goodDoc.data } returns mapOf(
            "id" to "t1",
            "name" to "Valid Template",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                )
            ),
            "createdAt" to "2025-11-22"
        )

        every { badDoc.id } returns "t2"
        every { badDoc.data } returns mapOf(
            "name" to "Invalid Template",
            "plan" to emptyList<Map<String, Any>>()
        )

        every { snapshot.documents } returns listOf(goodDoc, badDoc)

        val query = mockk<Query>()
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns query
        every { query.addSnapshotListener(capture(listenerSlot)) } answers {
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getTemplates().test(timeout = 2.seconds) {
            listenerSlot.captured.onEvent(snapshot, null)

            val templates = awaitItem()
            assertEquals(1, templates.size)
            assertEquals("Valid Template", templates[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchTemplatesOnce returns parsed templates`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()

        every { doc1.id } returns "t1"
        every { doc1.data } returns mapOf(
            "id" to "t1",
            "name" to "Template 1",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "squat",
                    "sets" to 4,
                    "weight" to 120.0
                )
            ),
            "createdAt" to "2025-11-22"
        )

        every { doc2.id } returns "t2"
        every { doc2.data } returns mapOf(
            "id" to "t2",
            "name" to "Template 2",
            "plan" to listOf(
                mapOf(
                    "exercise" to "tricep-extension",
                    "sets" to 3,
                    "weight" to 140.0
                )
            ),
            "createdAt" to "2025-11-21"
        )

        every { snapshot.documents } returns listOf(doc1, doc2)

        val query = mockk<Query>()
        every { templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns query
        every { query.limit(50) } returns query
        coEvery { query.get() } returns Tasks.forResult(snapshot)

        val result = repository.fetchTemplatesOnce()

        assertEquals(2, result.size)
        assertEquals("Template 1", result[0].name)
        assertEquals("Template 2", result[1].name)
        assertEquals(2, result[0].plan.items.size)
        assertEquals(1, result[1].plan.items.size)
    }

    @Test
    fun `fetchTemplatesOnce returns empty list on error`() = runTest {
        val query = mockk<Query>()
        every { templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns query
        every { query.limit(50) } returns query
        coEvery { query.get() } returns Tasks.forException(Exception("Network error"))

        val result = repository.fetchTemplatesOnce()

        assertEquals(0, result.size)
    }

    @Test
    fun `fetchTemplatesOnce respects limit parameter`() = runTest {
        val snapshot = mockk<QuerySnapshot>(relaxed = true)
        every { snapshot.documents } returns emptyList()

        val query = mockk<Query>()
        every { templatesCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns query
        every { query.limit(10) } returns query
        coEvery { query.get() } returns Tasks.forResult(snapshot)

        repository.fetchTemplatesOnce(limit = 10)

        verify { query.limit(10) }
    }

    @Test
    fun `deleteTemplate deletes template successfully`() = runTest {
        val docRef = mockk<DocumentReference>()
        every { templatesCollection.document("t1") } returns docRef
        coEvery { docRef.delete() } returns Tasks.forResult(null)

        repository.deleteTemplate("t1")

        coVerify { docRef.delete() }
    }

    @Test
    fun `deleteTemplate throws when no user logged in`() = runTest {
        every { auth.currentUser } returns null
        repository = WorkoutTemplateRepository(firestore, auth)

        assertFailsWith<IllegalStateException> {
            repository.deleteTemplate("t1")
        }
    }

    @Test
    fun `fetchTemplateByName returns template when found`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()

        every { doc.id } returns "t1"
        every { doc.data } returns mapOf(
            "id" to "t1",
            "name" to "Push Day",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                )
            ),
            "createdAt" to "2025-11-22"
        )

        every { snapshot.documents } returns listOf(doc)

        val query = mockk<Query>()
        every { templatesCollection.whereEqualTo("name", "Push Day") } returns query
        every { query.limit(1) } returns query
        coEvery { query.get() } returns Tasks.forResult(snapshot)

        val result = repository.fetchTemplateByName("Push Day")

        assertEquals("Push Day", result?.name)
        assertEquals("t1", result?.id)
        assertEquals(1, result?.plan?.items?.size)
        assertEquals(ExerciseType.BENCH_PRESS, result?.plan?.items?.get(0)?.exercise)
    }

    @Test
    fun `fetchTemplateByName returns null when not found`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { snapshot.documents } returns emptyList()

        val query = mockk<Query>()
        every { templatesCollection.whereEqualTo("name", "Nonexistent") } returns query
        every { query.limit(1) } returns query
        coEvery { query.get() } returns Tasks.forResult(snapshot)

        val result = repository.fetchTemplateByName("Nonexistent")

        assertNull(result)
    }

    @Test
    fun `fetchTemplateByName returns null on error`() = runTest {
        val query = mockk<Query>()
        every { templatesCollection.whereEqualTo("name", "Test") } returns query
        every { query.limit(1) } returns query
        coEvery { query.get() } returns Tasks.forException(Exception("Network error"))

        val result = repository.fetchTemplateByName("Test")

        assertNull(result)
    }

    @Test
    fun `fetchTemplateByName returns null when document data is invalid`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()

        every { doc.id } returns "t1"
        every { doc.data } returns mapOf(
            "name" to "Invalid",
            "plan" to emptyList<Map<String, Any>>()
        )

        every { snapshot.documents } returns listOf(doc)

        val query = mockk<Query>()
        every { templatesCollection.whereEqualTo("name", "Invalid") } returns query
        every { query.limit(1) } returns query
        coEvery { query.get() } returns Tasks.forResult(snapshot)

        val result = repository.fetchTemplateByName("Invalid")

        assertNull(result)
    }
}