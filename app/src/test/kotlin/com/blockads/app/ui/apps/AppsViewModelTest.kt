package com.blockads.app.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import app.cash.turbine.test
import com.blockads.app.core.data.settings.SettingsRepository
import com.blockads.app.domain.model.AppSettings
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModelTest {
    private lateinit var viewModel: AppsViewModel
    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mockSettingsFlow: MutableStateFlow<AppSettings>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        every { context.packageManager } returns packageManager
        every { context.packageName } returns "com.blockads.app"

        mockSettingsFlow = MutableStateFlow(AppSettings.default())
        every { settingsRepository.appSettings } returns mockSettingsFlow
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `appsUiState combines installed apps with bypassed apps correctly`() =
        runTest {
            val appInfo1 =
                ApplicationInfo().apply {
                    packageName = "com.example.app1"
                    flags = ApplicationInfo.FLAG_SYSTEM
                }
            val appInfo2 =
                ApplicationInfo().apply {
                    packageName = "com.example.app2"
                }
            val selfApp =
                ApplicationInfo().apply {
                    packageName = "com.blockads.app"
                }

            every { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(appInfo1, appInfo2, selfApp)
            every { packageManager.getApplicationLabel(appInfo1) } returns "App 1"
            every { packageManager.getApplicationLabel(appInfo2) } returns "App 2"
            // Drawable mocking is relaxed, returns null by default

            mockSettingsFlow.value =
                AppSettings.default().copy(
                    bypassedApps = setOf("com.example.app1"),
                )

            viewModel = AppsViewModel(context, settingsRepository)

            viewModel.appsUiState.test {
                var state = awaitItem()
                if (state.isEmpty()) {
                    state = awaitItem()
                }
                // Self app is filtered out
                assertEquals(2, state.size)

                // Sorted by name
                assertEquals("App 1", state[0].first.name)
                assertTrue(state[0].first.isSystemApp)
                assertTrue(state[0].second) // isBypassed

                assertEquals("App 2", state[1].first.name)
                assertFalse(state[1].first.isSystemApp)
                assertFalse(state[1].second) // isBypassed
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleAppBypass adds to bypassed set if checked`() =
        runTest {
            every { packageManager.getInstalledApplications(any<Int>()) } returns emptyList()
            mockSettingsFlow.value =
                AppSettings.default().copy(
                    bypassedApps = emptySet(),
                )

            viewModel = AppsViewModel(context, settingsRepository)
            viewModel.toggleAppBypass("com.test.app", true)

            coVerify { settingsRepository.setBypassedApps(setOf("com.test.app")) }
        }

    @Test
    fun `toggleAppBypass removes from bypassed set if unchecked`() =
        runTest {
            every { packageManager.getInstalledApplications(any<Int>()) } returns emptyList()
            mockSettingsFlow.value =
                AppSettings.default().copy(
                    bypassedApps = setOf("com.test.app", "com.other.app"),
                )

            viewModel = AppsViewModel(context, settingsRepository)
            viewModel.toggleAppBypass("com.test.app", false)

            coVerify { settingsRepository.setBypassedApps(setOf("com.other.app")) }
        }
}
