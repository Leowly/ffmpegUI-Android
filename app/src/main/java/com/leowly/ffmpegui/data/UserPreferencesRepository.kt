package com.leowly.ffmpegui.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val mode: String? = null, // Mode can be null if not set
    val serverAddress: String = "",
    val username: String = "",
    val password: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val MODE = stringPreferencesKey("mode")
        val SERVER_ADDRESS = stringPreferencesKey("server_address")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password") // Note: For production, encrypt this!
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            // When preferences are empty, MODE will be null, which is the correct initial state.
            val mode = preferences[Keys.MODE]
            val serverAddress = preferences[Keys.SERVER_ADDRESS] ?: ""
            val username = preferences[Keys.USERNAME] ?: ""
            val password = preferences[Keys.PASSWORD] ?: ""
            UserPreferences(mode, serverAddress, username, password)
        }

    suspend fun saveCloudPreferences(serverAddress: String, username: String, password: String) {
        context.dataStore.edit {
            it[Keys.MODE] = "cloud"
            it[Keys.SERVER_ADDRESS] = serverAddress
            it[Keys.USERNAME] = username
            it[Keys.PASSWORD] = password
        }
    }

    suspend fun saveLocalMode() {
        context.dataStore.edit {
            it[Keys.MODE] = "local"
            // You might want to clear server info when switching to local
            it.remove(Keys.SERVER_ADDRESS)
            it.remove(Keys.USERNAME)
            it.remove(Keys.PASSWORD)
        }
    }
}
