package com.leowly.ffmpegui.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme

@Composable
fun FileProcessingScreen() {
    // TODO: Implement the file processing screen.
    // This will be the main interface for users to input file paths and ffmpeg arguments.
    Text(stringResource(id = R.string.bottom_nav_file_processing))
}

@Preview
@Composable
fun FileProcessingScreenPreview() {
    FfmpegUITheme {
        FileProcessingScreen()
    }
}
