package com.leowly.ffmpegui.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.data.UserPreferencesRepository
import com.leowly.ffmpegui.http.HttpClient
import com.leowly.ffmpegui.http.TokenRequest
import com.leowly.ffmpegui.ui.components.ServerForm
import kotlinx.coroutines.launch

@Composable
fun AddServerScreen(navController: NavController) {
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    var serverAddress by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold {
        Column(modifier = Modifier.padding(it).padding(16.dp)) {
            Text(text = stringResource(R.string.add_server), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            ServerForm(
                serverAddress = serverAddress,
                onServerAddressChange = { serverAddress = it },
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = "", // Not needed for login
                onConfirmPasswordChange = {},
                isRegister = false,
                onToggleMode = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        error = null
                        scope.launch {
                            val loginResult = HttpClient.login(serverAddress, TokenRequest(username, password))
                            loginResult.fold(
                                onSuccess = { tokenResponse ->
                                    tokenResponse.data?.accessToken?.let { accessToken ->
                                        scope.launch {
                                            userPreferencesRepository.saveCloudLogin(
                                                serverAddress = serverAddress,
                                                username = username,
                                                password = password, // You might want to reconsider saving the password
                                                accessToken = accessToken
                                            )
                                            isLoading = false
                                            navController.popBackStack()
                                        }
                                    } ?: run {
                                        error = "Login successful, but no access token was provided."
                                        isLoading = false
                                    }
                                },
                                onFailure = { exception ->
                                    error = exception.message ?: "An unknown error occurred."
                                    isLoading = false
                                }
                            )
                        }
                    },
                    enabled = serverAddress.isNotBlank() && username.isNotBlank() && password.isNotBlank()
                ) {
                    Text(stringResource(R.string.add_and_login))
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
