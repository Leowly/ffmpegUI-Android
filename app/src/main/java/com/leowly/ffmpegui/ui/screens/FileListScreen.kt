package com.leowly.ffmpegui.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leowly.ffmpegui.R
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme

@Composable
fun FileListScreen() {
    // TODO: Implement the file list screen.
    // This screen will display a list of files from the local device or the remote server.
    Text(stringResource(id = R.string.bottom_nav_file_list))
}

@Preview
@Composable
fun FileListScreenPreview() {
    FfmpegUITheme {
        FileListScreen()
    }
}
