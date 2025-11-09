package com.leowly.ffmpegui.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme

@Composable
fun FileDetailsScreen() {
    // TODO: Implement the file details screen.
    // This screen will show detailed information about a selected file.
    Text(stringResource(id = R.string.bottom_nav_file_details))
}

@Preview
@Composable
fun FileDetailsScreenPreview() {
    FfmpegUITheme {
        FileDetailsScreen()
    }
}
