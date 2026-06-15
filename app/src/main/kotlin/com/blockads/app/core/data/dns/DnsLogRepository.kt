package com.blockads.app.core.data.dns

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsLogRepository
    @Inject
    constructor() {
        private val maxLogs = 500
        private val _logs = MutableStateFlow<List<DnsLogEntry>>(emptyList())
        val logs: StateFlow<List<DnsLogEntry>> = _logs.asStateFlow()

        fun logQuery(
            domain: String,
            isBlocked: Boolean,
        ) {
            val entry =
                DnsLogEntry(
                    timestampMs = System.currentTimeMillis(),
                    domain = domain,
                    isBlocked = isBlocked,
                )

            _logs.update { current ->
                val updated = current.toMutableList()
                updated.add(0, entry) // Add to top
                if (updated.size > maxLogs) {
                    updated.removeAt(updated.size - 1)
                }
                updated
            }
        }

        fun clearLogs() {
            _logs.value = emptyList()
        }
    }
