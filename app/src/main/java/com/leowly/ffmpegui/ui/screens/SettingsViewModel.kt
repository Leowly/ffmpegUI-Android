package com.leowly.ffmpegui.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leowly.ffmpegui.data.UserPreferences
import com.leowly.ffmpegui.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    val userPreferencesFlow: StateFlow<UserPreferences?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setMode(newMode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setMode(newMode)
        }
    }

    fun setActiveServer(serverId: String) {
        viewModelScope.launch {
            userPreferencesRepository.setActiveServer(serverId)
        }
    }

    fun removeServer(serverId: String) {
        viewModelScope.launch {
            userPreferencesRepository.removeServer(serverId)
        }
    }
}

class SettingsViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}