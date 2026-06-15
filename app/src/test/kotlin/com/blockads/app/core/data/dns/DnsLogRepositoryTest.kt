package com.blockads.app.core.data.dns

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DnsLogRepositoryTest {
    private lateinit var repository: DnsLogRepository

    @BeforeEach
    fun setup() {
        repository = DnsLogRepository()
    }

    @Test
    fun `initial state is empty`() =
        runTest {
            repository.logs.test {
                val initial = awaitItem()
                assertTrue(initial.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `logging a query adds it to the top of the list`() =
        runTest {
            repository.logQuery("example.com", isBlocked = false)
            repository.logQuery("blocked.com", isBlocked = true)

            repository.logs.test {
                val logs = awaitItem()
                assertEquals(2, logs.size)
                // Most recent is first
                assertEquals("blocked.com", logs[0].domain)
                assertTrue(logs[0].isBlocked)

                assertEquals("example.com", logs[1].domain)
                assertTrue(!logs[1].isBlocked)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `repository respects max logs limit of 500`() =
        runTest {
            for (i in 1..505) {
                repository.logQuery("domain$i.com", false)
            }

            repository.logs.test {
                val logs = awaitItem()
                assertEquals(500, logs.size)
                // Most recent should be domain505.com
                assertEquals("domain505.com", logs[0].domain)
                // Oldest should be domain6.com (1 to 5 dropped)
                assertEquals("domain6.com", logs[logs.size - 1].domain)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `clear logs empties the repository`() =
        runTest {
            repository.logQuery("test.com", false)
            repository.clearLogs()

            repository.logs.test {
                val logs = awaitItem()
                assertTrue(logs.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
