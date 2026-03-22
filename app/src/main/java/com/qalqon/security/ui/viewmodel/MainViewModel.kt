package com.qalqon.security.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qalqon.security.data.local.SecurityEvent
import com.qalqon.security.data.repo.SecurityRepository
import com.qalqon.security.settings.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: SecurityRepository,
    private val preferences: UserPreferences,
) : ViewModel() {
    val events: StateFlow<List<SecurityEvent>> =
        repository.observeEvents().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val darkModeEnabled: StateFlow<Boolean> =
        preferences.darkModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val monitoringEnabled: StateFlow<Boolean> =
        preferences.monitoringEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val alertsEnabled: StateFlow<Boolean> =
        preferences.alertsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setMonitoringEnabled(enabled) }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setAlertsEnabled(enabled) }
    }

    fun logLinkEvent(url: String, risky: Boolean, reason: String) {
        viewModelScope.launch {
            repository.logEvent(
                SecurityEvent(
                    eventType = "LINK_SCAN",
                    filePath = null,
                    sourceLabel = "User input",
                    severity = if (risky) "WARNING" else "SAFE",
                    message = "Link: $url | $reason",
                ),
            )
        }
    }
}

class MainViewModelFactory(
    private val repository: SecurityRepository,
    private val preferences: UserPreferences,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
