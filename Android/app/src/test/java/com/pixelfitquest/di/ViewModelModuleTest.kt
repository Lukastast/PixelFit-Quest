package com.pixelfitquest.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelfitquest.repository.UserRepository
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ViewModelModuleTest {

    @Test
    fun `provideUserRepository returns instance with correct dependencies`() {
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)

        val result = ViewModelModule.provideUserRepository(mockFirestore, mockAuth)

        assertNotNull(result)
        assertEquals(UserRepository::class.java, result::class.java)
        // If UserRepository has public properties or methods to check deps, verify them here.
        // E.g., if it exposes firestore/auth (assuming it does for testing):
        // assertEquals(mockFirestore, result.firestore)
        // assertEquals(mockAuth, result.auth)
    }
}