package com.blockads.app.ui.logs

import com.blockads.app.core.data.dns.DnsLogEntry
import com.blockads.app.core.data.dns.DnsLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LogsViewModelTest {
    private lateinit var viewModel: LogsViewModel
    private lateinit var dnsLogRepository: DnsLogRepository
    private lateinit var mockLogsFlow: MutableStateFlow<List<DnsLogEntry>>

    @BeforeEach
    fun setup() {
        dnsLogRepository = mockk(relaxed = true)

        mockLogsFlow = MutableStateFlow(emptyList())
        every { dnsLogRepository.logs } returns mockLogsFlow

        viewModel = LogsViewModel(dnsLogRepository)
    }

    @Test
    fun `logs state flow emits values from repository`() {
        val entry = DnsLogEntry(timestampMs = 12345L, domain = "test.com", isBlocked = true)
        mockLogsFlow.value = listOf(entry)

        val currentState = viewModel.logs.value
        assertEquals(1, currentState.size)
        assertEquals("test.com", currentState[0].domain)
    }

    @Test
    fun `clearLogs delegates to repository`() {
        viewModel.clearLogs()
        verify { dnsLogRepository.clearLogs() }
    }
}
