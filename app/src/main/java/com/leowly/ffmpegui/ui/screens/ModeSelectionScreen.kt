package com.leowly.ffmpegui.ui.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.http.HttpClient
import com.leowly.ffmpegui.http.TokenRequest
import com.leowly.ffmpegui.http.UserCreateRequest
import com.leowly.ffmpegui.ui.components.ServerForm
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme
import kotlinx.coroutines.launch

@Composable
fun ModeSelectionScreen() {
    var mode by remember { mutableStateOf("local") }
    var serverAddress by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.03f))

        Column(
            modifier = Modifier.weight(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModeCard(
                    selected = mode == "cloud",
                    onClick = { mode = "cloud" },
                    label = stringResource(R.string.cloud_mode)
                )

                AnimatedVisibility(
                    visible = mode == "cloud",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 24.dp)) {
                        ServerForm(
                            serverAddress = serverAddress,
                            onServerAddressChange = { serverAddress = it },
                            username = username,
                            onUsernameChange = { username = it },
                            password = password,
                            onPasswordChange = { password = it },
                            confirmPassword = confirmPassword,
                            onConfirmPasswordChange = { confirmPassword = it },
                            isRegister = isRegister,
                            onToggleMode = { isRegister = !isRegister }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ModeCard(
                    selected = mode == "local",
                    onClick = { mode = "local" },
                    label = stringResource(R.string.local_mode)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    when (mode) {
                        "cloud" -> {
                            coroutineScope.launch {
                                if (serverAddress.isBlank()) {
                                    Toast.makeText(context, "Server address cannot be empty.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                if (username.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                if (isRegister) {
                                    if (password != confirmPassword) {
                                        Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    if (password.length !in 8..72) {
                                        Toast.makeText(context, "Password must be between 8 and 72 characters.", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    if (!password.any { it.isLowerCase() }) {
                                        Toast.makeText(context, "密码必须包含至少一个小写字母", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    if (!password.any { it.isUpperCase() }) {
                                        Toast.makeText(context, "密码必须包含至少一个大写字母", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    if (!password.any { it.isDigit() }) {
                                        Toast.makeText(context, "密码必须包含至少一个数字", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    val request = UserCreateRequest(username, password)
                                    val result = HttpClient.register(serverAddress, request)
                                    result.onSuccess { user ->
                                        Toast.makeText(context, "Successfully registered user: ${user.username}", Toast.LENGTH_LONG).show()
                                        // Switch back to login mode after successful registration
                                        isRegister = false
                                    }.onFailure { exception ->
                                        val errorMessage = exception.message ?: "An unknown registration error occurred."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    val result = HttpClient.login(serverAddress, TokenRequest(username, password))
                                    result.onSuccess { apiResponse ->
                                        Toast.makeText(context, apiResponse.message, Toast.LENGTH_LONG).show()
                                    }.onFailure { exception ->
                                        val errorMessage = exception.message ?: "An unknown error occurred. Please check the server address and your connection."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        "local" -> {
                            // TODO: Handle local mode
                            Toast.makeText(context, "Continuing in local mode", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val buttonText = when (mode) {
                    "cloud" if isRegister -> R.string.register_button
                    "cloud" if true -> R.string.login_button
                    else -> R.string.continue_button
                }
                Text(stringResource(buttonText))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeCard(selected: Boolean, onClick: () -> Unit, label: String) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ModeSelectionScreenPreview() {
    FfmpegUITheme {
        ModeSelectionScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ModeSelectionScreenDarkPreview() {
    FfmpegUITheme {
        ModeSelectionScreen()
    }
}
