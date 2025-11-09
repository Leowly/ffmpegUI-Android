package com.leowly.ffmpegui.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme

@Composable
fun SettingsScreen() {
    // TODO: Implement the settings screen.
    // This screen will allow users to switch between local and cloud mode,
    // manage server addresses, and handle user accounts.
    Text(stringResource(id = R.string.bottom_nav_settings))
}

@Preview
@Composable
fun SettingsScreenPreview() {
    FfmpegUITheme {
        SettingsScreen()
    }
}
