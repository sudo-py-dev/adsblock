package com.blockads.app.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockads.app.core.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppItem(
    val packageName: String,
    val name: String,
    val icon: ImageBitmap?,
    val isSystemApp: Boolean,
)

@HiltViewModel
class AppsViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val installedAppsState = MutableStateFlow<List<AppItem>>(emptyList())
        private val isLoadingState = MutableStateFlow(true)
        val isLoading = isLoadingState.asStateFlow()

        private val bypassedAppsFlow =
            settingsRepository.appSettings
                .combine(installedAppsState) { settings, _ ->
                    settings.bypassedApps
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

        val appsUiState =
            combine(installedAppsState, bypassedAppsFlow) { apps, bypassed ->
                apps.map { app ->
                    app to bypassed.contains(app.packageName)
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            loadApps()
        }

        private fun loadApps() {
            viewModelScope.launch(Dispatchers.IO) {
                isLoadingState.value = true
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val appList =
                    packages.mapNotNull { info ->
                        if (info.packageName == context.packageName) return@mapNotNull null // Skip our own app
                        val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val name = pm.getApplicationLabel(info).toString()
                        val icon =
                            try {
                                pm.getApplicationIcon(info).toBitmap(width = 96, height = 96).asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        AppItem(
                            packageName = info.packageName,
                            name = name,
                            icon = icon,
                            isSystemApp = isSystem,
                        )
                    }.sortedBy { it.name.lowercase() }

                installedAppsState.value = appList
                isLoadingState.value = false
            }
        }

        fun toggleAppBypass(
            packageName: String,
            bypass: Boolean,
        ) {
            viewModelScope.launch {
                val current = settingsRepository.appSettings.first().bypassedApps.toMutableSet()
                if (bypass) {
                    current.add(packageName)
                } else {
                    current.remove(packageName)
                }
                settingsRepository.setBypassedApps(current)
            }
        }
    }
