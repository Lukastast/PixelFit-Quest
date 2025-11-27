package com.pixelfitquest.model.service.impl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserInfo
import com.pixelfitquest.model.User
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AccountServiceImplTest {

    private lateinit var accountService: AccountServiceImpl
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setUp() {
        mockkStatic(FirebaseAuth::class)
        mockAuth = mockk(relaxed = true)
        every { FirebaseAuth.getInstance() } returns mockAuth

        mockUser = mockk(relaxed = true)

        // Mock Google & Email providers
        mockkStatic(GoogleAuthProvider::class)
        mockkStatic(EmailAuthProvider::class)
        val mockCredential = mockk<AuthCredential>()
        every { GoogleAuthProvider.getCredential(any(), any()) } returns mockCredential
        every { EmailAuthProvider.getCredential(any(), any()) } returns mockCredential

        accountService = AccountServiceImpl()
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `currentUserId returns uid when user is logged in`() {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-uid-123"

        assertEquals("test-uid-123", accountService.currentUserId)
    }

    @Test
    fun `currentUserId returns empty string when no user`() {
        every { mockAuth.currentUser } returns null
        assertEquals("", accountService.currentUserId)
    }

    @Test
    fun `hasUser returns true when user exists`() {
        every { mockAuth.currentUser } returns mockUser
        assertTrue(accountService.hasUser())
    }

    @Test
    fun `hasUser returns false when no user exists`() {
        every { mockAuth.currentUser } returns null
        assertFalse(accountService.hasUser())
    }

    @Test
    fun `getUserProfile returns user data when logged in`() {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "https://photo.jpg"

        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"
        every { mockUser.email } returns "test@example.com"
        every { mockUser.displayName } returns "Test User"
        every { mockUser.photoUrl } returns mockUri
        every { mockUser.providerData } returns listOf(
            mockk<UserInfo> { every { providerId } returns "google.com" }
        )

        val user = accountService.getUserProfile()

        assertEquals("user-123", user.id)
        assertEquals("test@example.com", user.email)
        assertTrue(user.isLinkedWithGoogle)
    }

    @Test
    fun `getUserProfile returns empty user when not logged in`() {
        every { mockAuth.currentUser } returns null
        assertEquals(User(), accountService.getUserProfile())
    }

    @Test
    fun `updateDisplayName updates user profile`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        val mockTask = mockk<Task<Void>>()
        every { mockUser.updateProfile(any()) } returns mockTask
        coEvery { mockTask.await() } returns mockk()

        accountService.updateDisplayName("New Name")

        verify { mockUser.updateProfile(any()) }
    }

    @Test
    fun `linkAccountWithGoogle links credential`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        val mockTask = mockk<Task<AuthResult>>()
        every { mockUser.linkWithCredential(any()) } returns mockTask
        coEvery { mockTask.await() } returns mockk()

        accountService.linkAccountWithGoogle("test-token")

        verify { mockUser.linkWithCredential(any()) }
    }

    @Test
    fun `signInWithGoogle signs in with Google credential`() = runTest {
        val mockTask = mockk<Task<AuthResult>>()
        every { mockAuth.signInWithCredential(any()) } returns mockTask
        coEvery { mockTask.await() } returns mockk()

        accountService.signInWithGoogle("test-token")

        verify { mockAuth.signInWithCredential(any()) }
    }
    @Test
    fun `createAccountWithEmail creates account`() = runTest {
        val mockAuthResult = mockk<AuthResult>()
        val mockTask = mockk<Task<AuthResult>>()

        // CORRECT WAY: mock the extension function via mockkStatic
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery {
            mockTask.await()
        } returns mockAuthResult

        every {
            mockAuth.createUserWithEmailAndPassword("test@example.com", "pass123")
        } returns mockTask

        accountService.createAccountWithEmail("test@example.com", "pass123")

        verify {
            mockAuth.createUserWithEmailAndPassword("test@example.com", "pass123")
        }
    }

    @Test
    fun `signOut signs out user`() = runTest {
        every { mockAuth.signOut() } just Runs

        accountService.signOut()  // ← now inside runTest

        verify { mockAuth.signOut() }
    }

    @Test
    fun `deleteAccount deletes current user`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        val mockTask = mockk<Task<Void>>()
        every { mockUser.delete() } returns mockTask
        coEvery { mockTask.await() } returns mockk()

        accountService.deleteAccount()

        verify { mockUser.delete() }
    }

    @Test
    fun `currentUser flow emits user when auth state changes`() = runTest {
        var capturedListener: FirebaseAuth.AuthStateListener? = null

        // Capture the listener manually
        every { mockAuth.addAuthStateListener(any()) } answers {
            capturedListener = firstArg()
        }
        every { mockAuth.removeAuthStateListener(any()) } just Runs

        // Mock user data
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"
        every { mockUser.email } returns "test@example.com"
        every { mockUser.providerId } returns "firebase"
        every { mockUser.displayName } returns "Test User"
        every { mockUser.photoUrl } returns null
        every { mockUser.providerData } returns emptyList()

        // Create the service
        val service = AccountServiceImpl()

        val job = launch {
            val user = service.currentUser.first()
            assertEquals("user-123", user?.id)
            assertEquals("test@example.com", user?.email)
        }

        // Wait for listener to be registered
        testScheduler.advanceUntilIdle()

        // Trigger the listener
        capturedListener?.onAuthStateChanged(mockAuth)

        job.join()

        verify { mockAuth.addAuthStateListener(any()) }
    }

    @Test
    fun `currentUser flow emits empty user when no user logged in`() = runTest {
        var capturedListener: FirebaseAuth.AuthStateListener? = null

        // Capture the listener manually
        every { mockAuth.addAuthStateListener(any()) } answers {
            capturedListener = firstArg()
        }
        every { mockAuth.removeAuthStateListener(any()) } just Runs
        every { mockAuth.currentUser } returns null

        // Create the service
        val service = AccountServiceImpl()

        val job = launch {
            val user = service.currentUser.first()
            assertEquals(User(), user)
        }

        // Wait for listener to be registered
        testScheduler.advanceUntilIdle()

        // Trigger the listener
        capturedListener?.onAuthStateChanged(mockAuth)

        job.join()
    }
}