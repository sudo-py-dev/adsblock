package com.blockads.app.ui.logs

import androidx.lifecycle.ViewModel
import com.blockads.app.core.data.dns.DnsLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogsViewModel
    @Inject
    constructor(
        private val dnsLogRepository: DnsLogRepository,
    ) : ViewModel() {
        val logs = dnsLogRepository.logs

        fun clearLogs() {
            dnsLogRepository.clearLogs()
        }
    }
