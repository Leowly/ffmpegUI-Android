package com.leowly.ffmpegui.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme

@Composable
fun ServerForm(
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isRegister: Boolean,
    onToggleMode: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val textFieldColors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            cursorColor = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = serverAddress,
            onValueChange = onServerAddressChange,
            label = { Text(stringResource(R.string.server_address)) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.username)) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        AnimatedVisibility(
            visible = isRegister,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.confirm_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(if (isRegister) R.string.has_account_login else R.string.no_account_register),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                onClick = onToggleMode,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ServerFormPreview() {
    FfmpegUITheme {
        ServerForm(
            serverAddress = "",
            onServerAddressChange = { },
            username = "",
            onUsernameChange = { },
            password = "",
            onPasswordChange = { },
            confirmPassword = "",
            onConfirmPasswordChange = { },
            isRegister = true,
            onToggleMode = { }
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ServerFormDarkPreview() {
    FfmpegUITheme {
        ServerForm(
            serverAddress = "",
            onServerAddressChange = { },
            username = "",
            onUsernameChange = { },
            password = "",
            onPasswordChange = { },
            confirmPassword = "",
            onConfirmPasswordChange = { },
            isRegister = false,
            onToggleMode = { }
        )
    }
}
