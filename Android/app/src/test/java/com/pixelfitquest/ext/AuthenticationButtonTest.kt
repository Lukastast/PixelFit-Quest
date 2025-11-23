package com.pixelfitquest.ext

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.pixelfitquest.helpers.ERROR_TAG
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.R
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AuthenticationButtonTest {

    private lateinit var mockContext: Context
    private lateinit var mockCredentialManager: CredentialManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockContext = mockk(relaxed = true)
        mockCredentialManager = mockk(relaxed = true)

        mockkStatic(Log::class)
        mockkObject(SnackbarManager)

        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { SnackbarManager.showMessage(any<String>()) } just Runs
        every { mockContext.getString(R.string.default_web_client_id) } returns "test_client_id"
        every { mockContext.getString(R.string.no_accounts_error) } returns "No accounts error"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `launchCredManButtonUI succeeds with credential`() = runTest {
        val mockCredential = mockk<Credential>()
        val mockResponse = mockk<GetCredentialResponse>()

        every { mockResponse.credential } returns mockCredential
        coEvery {
            mockCredentialManager.getCredential(
                request = any<GetCredentialRequest>(),
                context = any<Context>()
            )
        } returns mockResponse

        var result: Credential? = null
        launchCredManButtonUI(mockContext, mockCredentialManager) { result = it }

        assertNotNull(result)
        assertEquals(mockCredential, result)
    }

    @Test
    fun `launchCredManButtonUI handles NoCredentialException`() = runTest {
        val noCredException = NoCredentialException("No credentials found")

        coEvery {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        } throws noCredException

        var callbackInvoked = false
        launchCredManButtonUI(mockContext, mockCredentialManager) { callbackInvoked = true }

        assertEquals(false, callbackInvoked)
        verify { Log.d(ERROR_TAG, "No credentials found") }
        verify { SnackbarManager.showMessage("No accounts error") }
    }

    @Test
    fun `launchCredManButtonUI handles GetCredentialException`() = runTest {
        val getCredException = mockk<GetCredentialException>(relaxed = true) {
            every { message } returns "Error getting credentials"
        }

        coEvery {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        } throws getCredException

        var callbackInvoked = false
        launchCredManButtonUI(mockContext, mockCredentialManager) { callbackInvoked = true }

        assertEquals(false, callbackInvoked)
        verify { Log.d(ERROR_TAG, "Error getting credentials") }
    }

    @Test
    fun `launchCredManBottomSheet handles NoCredentialException and retries without filter`() = runTest {
        val noCredException = NoCredentialException("No credentials found")

        coEvery {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        } throws noCredException

        var callbackInvoked = false
        launchCredManBottomSheet(mockContext, hasFilter = true, mockCredentialManager) { callbackInvoked = true }

        coVerify(exactly = 2) {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        }

        assertEquals(false, callbackInvoked)
    }

    @Test
    fun `launchCredManBottomSheet succeeds on retry`() = runTest {
        val mockCredential = mockk<Credential>()
        val mockResponse = mockk<GetCredentialResponse>()
        val noCredException = NoCredentialException("No credentials found")

        every { mockResponse.credential } returns mockCredential
        coEvery {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        } throws noCredException andThen mockResponse

        var result: Credential? = null
        launchCredManBottomSheet(mockContext, hasFilter = true, mockCredentialManager) { result = it }

        assertNotNull(result)
        assertEquals(mockCredential, result)

        coVerify(exactly = 2) {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        }
    }

    @Test
    fun `launchCredManBottomSheet handles GetCredentialException`() = runTest {
        val getCredException = mockk<GetCredentialException>(relaxed = true) {
            every { message } returns "Error getting credentials"
        }

        coEvery {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        } throws getCredException

        var callbackInvoked = false
        launchCredManBottomSheet(mockContext, hasFilter = true, mockCredentialManager) { callbackInvoked = true }

        assertEquals(false, callbackInvoked)
        verify { Log.d(ERROR_TAG, "Error getting credentials") }

        coVerify(exactly = 1) {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        }
    }

    @Test
    fun `launchCredManBottomSheet without filter succeeds immediately`() = runTest {
        val mockCredential = mockk<Credential>()
        val mockResponse = mockk<GetCredentialResponse>()

        every { mockResponse.credential } returns mockCredential
        coEvery {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        } returns mockResponse

        var result: Credential? = null
        launchCredManBottomSheet(mockContext, hasFilter = false, mockCredentialManager) { result = it }

        assertNotNull(result)
        assertEquals(mockCredential, result)

        coVerify(exactly = 1) {
            mockCredentialManager.getCredential(
                request = any(),
                context = any()
            )
        }
    }
}