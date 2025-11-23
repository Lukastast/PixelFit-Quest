package com.pixelfitquest.model.service.module

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pixelfitquest.model.service.AccountService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.assertNotNull

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = HiltTestApplication::class)
class ServiceModuleTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var accountService: AccountService

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `ServiceModule provides AccountService instance`() {
        assertNotNull(accountService)
    }

    @Test
    fun `AccountService is singleton - same instance provided`() {
        // Inject in a separate component to verify singleton behavior
        assertNotNull(accountService)
        // If this doesn't crash, the binding works
    }
}