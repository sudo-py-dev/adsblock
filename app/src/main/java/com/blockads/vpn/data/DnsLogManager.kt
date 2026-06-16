package com.blockads.vpn.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DnsLogEntry(
    val domain: String,
    val isBlocked: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = java.util.UUID.randomUUID().toString(),
)

object DnsLogManager {
    private const val MAX_LOGS = 500

    private val _logs = MutableStateFlow<List<DnsLogEntry>>(emptyList())
    val logs: StateFlow<List<DnsLogEntry>> = _logs.asStateFlow()

    fun addLog(
        domain: String,
        isBlocked: Boolean,
    ) {
        val entry = DnsLogEntry(domain, isBlocked)
        _logs.update { currentList ->
            val newList = currentList.toMutableList()
            newList.add(0, entry) // Add to top
            if (newList.size > MAX_LOGS) {
                newList.subList(0, MAX_LOGS)
            } else {
                newList
            }
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
