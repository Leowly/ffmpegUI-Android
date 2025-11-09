package com.leowly.ffmpegui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.leowly.ffmpegui.ui.screens.ModeSelectionScreen
import com.leowly.ffmpegui.ui.theme.FfmpegUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FfmpegUITheme {
                ModeSelectionScreen()
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun AppPreview() {
    FfmpegUITheme {
        ModeSelectionScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppDarkPreview() {
    FfmpegUITheme {
        ModeSelectionScreen()
    }
}
