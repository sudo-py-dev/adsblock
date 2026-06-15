package com.blockads.app.ui.rules

import app.cash.turbine.test
import com.blockads.app.core.data.rules.CustomRule
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulesViewModelTest {
    private lateinit var viewModel: RulesViewModel
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mockSettingsFlow: MutableStateFlow<AppSettings>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)

        mockSettingsFlow = MutableStateFlow(AppSettings.default())
        every { settingsRepository.appSettings } returns mockSettingsFlow

        viewModel = RulesViewModel(settingsRepository)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rules state flow maps whitelist and blacklist correctly`() =
        runTest {
            mockSettingsFlow.value =
                AppSettings.default().copy(
                    whitelistDomains = setOf("allow.com"),
                    blacklistDomains = setOf("block.com"),
                )

            viewModel.rules.test {
                val rules = awaitItem()
                // Wait for initial value then update
                if (rules.isEmpty()) {
                    val updatedRules = awaitItem()
                    assertEquals(2, updatedRules.size)

                    // Should be sorted alphabetically: allow.com, block.com
                    assertEquals("allow.com", updatedRules[0].domain)
                    assertTrue(updatedRules[0].isWhitelist)

                    assertEquals("block.com", updatedRules[1].domain)
                    assertTrue(!updatedRules[1].isWhitelist)
                } else {
                    assertEquals(2, rules.size)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `addRule adds to whitelist and removes from blacklist`() =
        runTest {
            mockSettingsFlow.value =
                AppSettings.default().copy(
                    blacklistDomains = setOf("test.com"),
                )

            viewModel.addRule("Test.Com ", isWhitelist = true)
            viewModel.addRule("Test.Com ", isWhitelist = true)

            coVerify {
                settingsRepository.setWhitelistDomains(setOf("test.com"))
                settingsRepository.setBlacklistDomains(emptySet())
            }
        }

    @Test
    fun `removeRule removes from correct list`() =
        runTest {
            mockSettingsFlow.value =
                AppSettings.default().copy(
                    whitelistDomains = setOf("keep.com", "remove.com"),
                )

            viewModel.removeRule(CustomRule("remove.com", isWhitelist = true))
            viewModel.removeRule(CustomRule("remove.com", isWhitelist = true))

            coVerify {
                settingsRepository.setWhitelistDomains(setOf("keep.com"))
            }
        }
}
