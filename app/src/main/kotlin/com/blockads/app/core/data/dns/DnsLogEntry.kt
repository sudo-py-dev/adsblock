package com.blockads.app.core.data.dns

data class DnsLogEntry(
    val timestampMs: Long,
    val domain: String,
    val isBlocked: Boolean,
)
