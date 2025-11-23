package com.leowly.ffmpegui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leowly.ffmpegui.http.FileItem

@Composable
fun FileListScreen(viewModel: FileListViewModel = viewModel()) {
    val fileList by viewModel.fileList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "file_to_upload"
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val fileBytes = inputStream.readBytes()
                viewModel.uploadFile(fileName, fileBytes)
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(message = it)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(
                    onClick = { launcher.launch("*/*") },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload")
                    Text("Upload")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading && fileList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(fileList, key = { it.id }) { file ->
                        FileListItem(file = file, onDelete = { viewModel.deleteFile(it) })
                    }
                }
            }
        }
    }
}

@Composable
fun FileListItem(file: FileItem, onDelete: (String) -> Unit) {
    ListItem(
        headlineContent = { Text(file.name) },
        supportingContent = { Text("${(file.size / 1024 / 1024).toFloat().toBigDecimal().toPlainString()} MB") },
        trailingContent = {
            IconButton(onClick = { onDelete(file.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete File")
            }
        }
    )
}
