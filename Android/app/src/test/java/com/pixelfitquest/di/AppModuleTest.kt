package com.pixelfitquest.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import io.mockk.*
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppModuleTest {

    private lateinit var mockContext: Context
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockSharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)
        mockAuth = mockk(relaxed = true)
        mockSharedPreferences = mockk(relaxed = true)

        // Mock static/instance creations
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { FirebaseAuth.getInstance() } returns mockAuth
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `provideFirestore returns configured instance`() {
        val initialSettings = mockk<FirebaseFirestoreSettings>(relaxed = true)
        every { mockFirestore.firestoreSettings } returns initialSettings

        val slot = slot<FirebaseFirestoreSettings>()
        every { mockFirestore.firestoreSettings = capture(slot) } just Runs

        val result = AppModule.provideFirestore()

        assertNotNull(result)
        assertEquals(mockFirestore, result)

        verify { mockFirestore.firestoreSettings = any<FirebaseFirestoreSettings>() }
        assertTrue(slot.isCaptured)
        assertTrue(slot.captured.cacheSettings is PersistentCacheSettings)
    }

    @Test
    fun `provideFirebaseAuth returns singleton instance`() {
        val result = AppModule.provideFirebaseAuth()

        assertNotNull(result)
        assertEquals(mockAuth, result)
    }

    @Test
    fun `provideSharedPreferences returns default preferences`() {
        mockkStatic("android.preference.PreferenceManager")
        every { android.preference.PreferenceManager.getDefaultSharedPreferences(mockContext) } returns mockSharedPreferences

        val result = AppModule.provideSharedPreferences(mockContext)

        assertNotNull(result)
        assertEquals(mockSharedPreferences, result)
        verify { android.preference.PreferenceManager.getDefaultSharedPreferences(mockContext) }
    }
}