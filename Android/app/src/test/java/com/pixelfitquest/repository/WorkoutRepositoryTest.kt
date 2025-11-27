package com.pixelfitquest.repository

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.pixelfitquest.model.Exercise
import com.pixelfitquest.model.ExerciseType
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.Workout
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WorkoutRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var usersCollection: CollectionReference
    private lateinit var workoutsCollection: CollectionReference
    private lateinit var repository: WorkoutRepository

    private val uid = "test_user_123"

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.v(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.d(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        MockKAnnotations.init(this)

        firestore = mockk(relaxed = true)
        auth = mockk()
        user = mockk()
        usersCollection = mockk()
        workoutsCollection = mockk()

        every { auth.currentUser } returns user
        every { user.uid } returns uid

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(uid) } returns mockk(relaxed = true)
        every { usersCollection.document(uid).collection("workouts") } returns workoutsCollection

        repository = WorkoutRepository(firestore, auth)
    }

    @Test
    fun `getWorkouts flow emits empty list on error`() = runTest {
        val query = mockk<Query>()
        every { workoutsCollection.orderBy("date", Query.Direction.DESCENDING) } returns query

        every { query.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<QuerySnapshot>
            val fakeError = mockk<FirebaseFirestoreException>(relaxed = true)
            listener.onEvent(null, fakeError)
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getWorkouts().test {
            assertEquals(emptyList<Workout>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `getWorkouts flow emits parsed workouts`() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val docOlder = mockk<DocumentSnapshot>()  // 2025-11-20
        val docNewer = mockk<DocumentSnapshot>()  // 2025-11-22

        every { docOlder.data } returns mapOf(
            "id" to "w1",
            "date" to "2025-11-20",
            "name" to "Push Day",
            "totalExercises" to 5,
            "totalSets" to 20,
            "overallScore" to 0f,
            "notes" to null,
            "rewardsAwarded" to false
        )
        every { docNewer.data } returns mapOf(
            "id" to "w2",
            "date" to "2025-11-22",
            "name" to "Pull Day",
            "totalExercises" to 6,
            "totalSets" to 24,
            "overallScore" to 0f,
            "notes" to null,
            "rewardsAwarded" to false
        )

        // Return documents in sorted order (DESC = newest first)
        every { snapshot.documents } returns listOf(docNewer, docOlder)

        val query = mockk<Query>()

        // FIXED: Use "date" instead of "createdAt" to match your actual code
        every { workoutsCollection.orderBy("date", Query.Direction.DESCENDING) } returns query

        every { query.addSnapshotListener(any()) } answers {
            val listener = it.invocation.args[0] as EventListener<QuerySnapshot>
            listener.onEvent(snapshot, null)
            mockk<ListenerRegistration>(relaxed = true)
        }

        repository.getWorkouts().test {
            val workouts = awaitItem()
            assertEquals(2, workouts.size)
            assertEquals("Pull Day", workouts[0].name)   // newest first
            assertEquals("Push Day", workouts[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveWorkout saves correctly`() = runTest {
        val workout = Workout(
            id = "w999",
            date = "2025-11-22",
            name = "Leg Day",
            totalExercises = 6,
            totalSets = 24,
            overallScore = 92.5f,
            notes = "Felt amazing!",
            rewardsAwarded = true
        )

        val workoutDoc = mockk<DocumentReference>()
        every { workoutsCollection.document("w999") } returns workoutDoc

        val capturedMap = slot<Map<String, Any?>>()
        coEvery { workoutDoc.set(capture(capturedMap)) } returns Tasks.forResult(null)

        repository.saveWorkout(workout)

        with(capturedMap.captured) {
            assertEquals("w999", this["id"])
            assertEquals("Leg Day", this["name"])
            assertEquals(24, this["totalSets"])
            assertEquals("2025-11-22", this["date"])
            assertEquals(true, this["rewardsAwarded"])
            assertEquals("Felt amazing!", this["notes"])
            assertEquals(6, this["totalExercises"])
            assertEquals(92.5f, this["overallScore"])
        }
    }

    @Test
    fun `getExercisesByWorkoutId returns parsed exercises`() = runTest {
        val workoutId = "w123"
        val exercisesCollection = mockk<CollectionReference>()
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()

        every { workoutsCollection.document(workoutId).collection("exercises") } returns exercisesCollection
        coEvery { exercisesCollection.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc)
        every { doc.data } returns mapOf(
            "id" to "ex1",
            "workoutId" to workoutId,
            "type" to "squat",
            "totalSets" to 4,
            "weight" to 120.0f,
            "notes" to "Felt good"
        )

        val result = repository.getExercisesByWorkoutId(workoutId)

        assertEquals(1, result.size)
        assertEquals("ex1", result[0].id)
        assertEquals(workoutId, result[0].workoutId)
        assertEquals(ExerciseType.SQUAT, result[0].type)
        assertEquals(4, result[0].totalSets)
        assertEquals(120f, result[0].weight)
        assertEquals("Felt good", result[0].notes)
    }

    @Test
    fun `getSetsByWorkoutId loads sets from all exercises`() = runTest {
        repository = spyk(WorkoutRepository(firestore, auth))

        val workoutId = "w456"

        val ex1 = Exercise(
            id = "ex1",
            workoutId = workoutId,
            type = ExerciseType.BENCH_PRESS,
            totalSets = 3,
            weight = 100f,
            notes = "Felt good"
        )
        val ex2 = Exercise(
            id = "ex2",
            workoutId = workoutId,
            type = ExerciseType.SEATED_ROWS,
            totalSets = 4,
            weight = 80f
        )

        coEvery { repository.getExercisesByWorkoutId(workoutId) } returns listOf(ex1, ex2)

        val setsCol1 = mockk<CollectionReference>()
        val setsCol2 = mockk<CollectionReference>()
        val snap1 = mockk<QuerySnapshot>()
        val snap2 = mockk<QuerySnapshot>()
        val setDoc1 = mockk<DocumentSnapshot>()
        val setDoc2 = mockk<DocumentSnapshot>()

        every { workoutsCollection.document(workoutId).collection("exercises").document("ex1").collection("sets") } returns setsCol1
        every { workoutsCollection.document(workoutId).collection("exercises").document("ex2").collection("sets") } returns setsCol2

        coEvery { setsCol1.get() } returns Tasks.forResult(snap1)
        coEvery { setsCol2.get() } returns Tasks.forResult(snap2)

        every { snap1.documents } returns listOf(setDoc1)
        every { snap2.documents } returns listOf(setDoc2)

        // ADD THESE LINES - Mock the getId() method
        every { setDoc1.id } returns "set1"
        every { setDoc2.id } returns "set2"

        every { setDoc1.data } returns mapOf(
            "reps" to 10,
            "weight" to 100.0f
        )
        every { setDoc2.data } returns mapOf(
            "reps" to 8,
            "weight" to 80.0f
        )

        val result = repository.getSetsByWorkoutId(workoutId)

        assertEquals(2, result.size)
        assertEquals("ex1", result[0].exerciseId)
        assertEquals("ex2", result[1].exerciseId)
        assertEquals(100.0f, result[0].weight)
        assertEquals(10, result[0].reps)
        assertEquals(80.0f, result[1].weight)
        assertEquals(8, result[1].reps)
    }

    @Test
    fun `methods throw when no user logged in`() = runTest {
        every { auth.currentUser } returns null
        repository = WorkoutRepository(firestore, auth)

        // Use assertFailsWith for suspend functions and flows
        assertFailsWith<IllegalStateException> {
            repository.getWorkouts().first() // Collect the flow to trigger execution
        }
    }
}