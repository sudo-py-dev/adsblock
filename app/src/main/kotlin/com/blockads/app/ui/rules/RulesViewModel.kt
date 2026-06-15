package com.blockads.app.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockads.app.core.data.rules.CustomRule
import com.blockads.app.core.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val rules: StateFlow<List<CustomRule>> =
            settingsRepository.appSettings
                .map { settings ->
                    val wl = settings.whitelistDomains.map { CustomRule(it, isWhitelist = true) }
                    val bl = settings.blacklistDomains.map { CustomRule(it, isWhitelist = false) }
                    (wl + bl).sortedBy { it.domain }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        fun addRule(
            domain: String,
            isWhitelist: Boolean,
        ) {
            val trimmed = domain.trim().lowercase()
            if (trimmed.isEmpty()) return

            viewModelScope.launch {
                val settings = settingsRepository.appSettings.first()

                // Remove from the other list if it exists
                if (isWhitelist) {
                    val newWl = settings.whitelistDomains + trimmed
                    val newBl = settings.blacklistDomains - trimmed
                    settingsRepository.setWhitelistDomains(newWl)
                    settingsRepository.setBlacklistDomains(newBl)
                } else {
                    val newBl = settings.blacklistDomains + trimmed
                    val newWl = settings.whitelistDomains - trimmed
                    settingsRepository.setBlacklistDomains(newBl)
                    settingsRepository.setWhitelistDomains(newWl)
                }
            }
        }

        fun removeRule(rule: CustomRule) {
            viewModelScope.launch {
                val settings = settingsRepository.appSettings.first()
                if (rule.isWhitelist) {
                    settingsRepository.setWhitelistDomains(settings.whitelistDomains - rule.domain)
                } else {
                    settingsRepository.setBlacklistDomains(settings.blacklistDomains - rule.domain)
                }
            }
        }
    }
