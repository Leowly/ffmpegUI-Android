package com.leowly.ffmpegui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.data.ServerConfig
import com.leowly.ffmpegui.data.UserPreferencesRepository

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(UserPreferencesRepository(LocalContext.current))
    )
) {
    val userPreferences by viewModel.userPreferencesFlow.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selection
        ModeSelection(
            currentMode = userPreferences?.mode ?: "local",
            onModeChange = { newMode -> viewModel.setMode(newMode) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cloud specific settings
        if (userPreferences?.mode == "cloud") {
            CloudSettings(
                servers = userPreferences?.servers ?: emptyList(),
                activeServerId = userPreferences?.activeServerId,
                onSetActive = { serverId -> viewModel.setActiveServer(serverId) },
                onRemove = { serverId -> viewModel.removeServer(serverId) },
                onAddServer = { navController.navigate("add_server") }
            )
        }
    }
}

@Composable
private fun ModeSelection(currentMode: String, onModeChange: (String) -> Unit) {
    Column {
        Text(stringResource(R.string.mode), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = currentMode == "local",
                onClick = { onModeChange("local") }
            )
            Text(stringResource(R.string.local_mode))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = currentMode == "cloud",
                onClick = { onModeChange("cloud") }
            )
            Text(stringResource(R.string.cloud_mode))
        }
    }
}

@Composable
private fun CloudSettings(
    servers: List<ServerConfig>,
    activeServerId: String?,
    onSetActive: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddServer: () -> Unit
) {
    Column {
        Text(stringResource(R.string.servers), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (servers.isEmpty()) {
            Text(stringResource(R.string.no_servers_configured))
        } else {
            servers.forEach { server ->
                ServerItem(
                    server = server,
                    isActive = server.id == activeServerId,
                    onSetActive = { onSetActive(server.id) },
                    onRemove = { onRemove(server.id) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onAddServer) {
            Text(stringResource(R.string.add_server))
        }

        // Display active server info
        val activeServer = servers.find { it.id == activeServerId }
        if (activeServer != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.active_server), style = MaterialTheme.typography.titleMedium)
            Text("Username: ${activeServer.username}")
            Text("Server: ${activeServer.serverAddress}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerItem(
    server: ServerConfig,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onSetActive,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = server.username, style = MaterialTheme.typography.bodyLarge)
                Text(text = server.serverAddress, style = MaterialTheme.typography.bodySmall)
            }
            if (isActive) {
                Text(
                    text = stringResource(R.string.active),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_server))
            }
        }
    }
}