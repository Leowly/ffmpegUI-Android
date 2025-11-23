package com.leowly.ffmpegui.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Define the DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Serializable
data class ServerConfig(
    val id: String, // Unique ID for each server config, e.g., username@serverAddress
    val serverAddress: String,
    val username: String,
    val password: String, // Kept for potential re-login scenarios
    val accessToken: String
)

data class UserPreferences(
    val mode: String? = null, // "local" or "cloud"
    val servers: List<ServerConfig> = emptyList(),
    val activeServerId: String? = null
) {
    val activeServer: ServerConfig?
        get() = servers.find { it.id == activeServerId }
}

class UserPreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val MODE = stringPreferencesKey("mode")
        val SERVERS = stringPreferencesKey("servers_json")
        val ACTIVE_SERVER_ID = stringPreferencesKey("active_server_id")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val mode = preferences[Keys.MODE]
            val serversJson = preferences[Keys.SERVERS] ?: "[]"
            val servers = try {
                json.decodeFromString<List<ServerConfig>>(serversJson)
            } catch (e: Exception) {
                emptyList() // In case of deserialization error
            }
            val activeServerId = preferences[Keys.ACTIVE_SERVER_ID]
            UserPreferences(mode, servers, activeServerId)
        }

    /**
     * Saves all cloud mode preferences in a single, atomic transaction.
     * This now adds or updates a server and sets it as active.
     */
    suspend fun saveCloudLogin(serverAddress: String, username: String, password: String, accessToken: String) {
        val serverId = "$username@$serverAddress"
        context.dataStore.edit { preferences ->
            val serversJson = preferences[Keys.SERVERS] ?: "[]"
            val servers = try {
                json.decodeFromString<MutableList<ServerConfig>>(serversJson)
            } catch (e: Exception) {
                mutableListOf()
            }

            val existingServerIndex = servers.indexOfFirst { it.id == serverId }
            val newServer = ServerConfig(serverId, serverAddress, username, password, accessToken)

            if (existingServerIndex != -1) {
                servers[existingServerIndex] = newServer
            } else {
                servers.add(newServer)
            }

            preferences[Keys.MODE] = "cloud"
            preferences[Keys.SERVERS] = json.encodeToString(servers)
            preferences[Keys.ACTIVE_SERVER_ID] = serverId
        }
    }

    suspend fun saveLocalMode() {
        context.dataStore.edit {
            it[Keys.MODE] = "local"
            it.remove(Keys.ACTIVE_SERVER_ID)
        }
    }
    
    suspend fun setMode(newMode: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MODE] = newMode
        }
    }

    suspend fun setActiveServer(serverId: String) {
        context.dataStore.edit { preferences ->
            // Ensure the server exists before setting it as active
            val serversJson = preferences[Keys.SERVERS] ?: "[]"
            val servers = json.decodeFromString<List<ServerConfig>>(serversJson)
            if (servers.any { it.id == serverId }) {
                preferences[Keys.ACTIVE_SERVER_ID] = serverId
                preferences[Keys.MODE] = "cloud" // Switch to cloud mode if setting an active server
            }
        }
    }

    suspend fun removeServer(serverId: String) {
        context.dataStore.edit { preferences ->
            val serversJson = preferences[Keys.SERVERS] ?: "[]"
            val servers = json.decodeFromString<MutableList<ServerConfig>>(serversJson)
            
            servers.removeAll { it.id == serverId }
            preferences[Keys.SERVERS] = json.encodeToString(servers)

            // If the removed server was active
            if (preferences[Keys.ACTIVE_SERVER_ID] == serverId) {
                if (servers.isNotEmpty()) {
                    // Set the first remaining server as active
                    preferences[Keys.ACTIVE_SERVER_ID] = servers.first().id
                } else {
                    // If no servers are left, switch to local mode
                    preferences[Keys.MODE] = "local"
                    preferences.remove(Keys.ACTIVE_SERVER_ID)
                }
            }
        }
    }
}