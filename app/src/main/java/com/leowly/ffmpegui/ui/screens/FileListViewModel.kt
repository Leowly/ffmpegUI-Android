package com.leowly.ffmpegui.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leowly.ffmpegui.data.UserPreferencesRepository
import com.leowly.ffmpegui.http.FileItem
import com.leowly.ffmpegui.http.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FileListViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _fileList = MutableStateFlow<List<FileItem>>(emptyList())
    val fileList: StateFlow<List<FileItem>> = _fileList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchFileList()
    }

    fun fetchFileList() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val prefs = userPreferencesRepository.userPreferencesFlow.first()

            if (prefs.mode != "cloud") {
                _error.value = "Not in cloud mode."
                _isLoading.value = false
                _fileList.value = emptyList() // Clear file list
                return@launch
            }

            val activeServer = prefs.activeServer
            if (activeServer == null) {
                _error.value = "No active server selected."
                _isLoading.value = false
                return@launch
            }

            val serverAddress = activeServer.serverAddress
            val accessToken = activeServer.accessToken

            if (serverAddress.isBlank() || accessToken.isBlank()) {
                _error.value = "Server address or token is not configured."
                _isLoading.value = false
                return@launch
            }

            HttpClient.getFiles(serverAddress, accessToken)
                .onSuccess { files ->
                    _fileList.value = files
                }
                .onFailure { e ->
                    _error.value = "Failed to fetch files: ${e.message}"
                }

            _isLoading.value = false
        }
    }

    fun uploadFile(fileName: String, content: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val prefs = userPreferencesRepository.userPreferencesFlow.first()

            if (prefs.mode != "cloud") {
                _error.value = "Not in cloud mode."
                _isLoading.value = false
                return@launch
            }

            val activeServer = prefs.activeServer
            if (activeServer == null) {
                _error.value = "No active server selected."
                _isLoading.value = false
                return@launch
            }

            val serverAddress = activeServer.serverAddress
            val accessToken = activeServer.accessToken

            if (serverAddress.isBlank() || accessToken.isBlank()) {
                _error.value = "Server address or token is not configured."
                _isLoading.value = false
                return@launch
            }

            HttpClient.uploadFile(serverAddress, accessToken, fileName, content)
                .onSuccess {
                    // Refresh the list after successful upload
                    fetchFileList()
                }
                .onFailure {
                    _error.value = "Upload failed: ${it.message}"
                    _isLoading.value = false // Stop loading indicator on failure
                }
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            // Note: We don't set isLoading for delete, as it's a quick operation
            _error.value = null
            val prefs = userPreferencesRepository.userPreferencesFlow.first()

            if (prefs.mode != "cloud") {
                _error.value = "Not in cloud mode."
                return@launch
            }

            val activeServer = prefs.activeServer
            if (activeServer == null) {
                _error.value = "No active server selected."
                return@launch
            }

            val serverAddress = activeServer.serverAddress
            val accessToken = activeServer.accessToken

            if (serverAddress.isBlank() || accessToken.isBlank()) {
                _error.value = "Server address or token is not configured."
                return@launch
            }

            HttpClient.deleteFile(serverAddress, accessToken, fileId)
                .onSuccess {
                    // Refresh the list on successful deletion
                    _fileList.value = _fileList.value.filter { it.id != fileId }
                }
                .onFailure { e ->
                    _error.value = "Failed to delete file: ${e.message}"
                }
        }
    }
}
