package com.leowly.ffmpegui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.leowly.ffmpegui.data.UserPreferences
import com.leowly.ffmpegui.data.UserPreferencesRepository
import com.leowly.ffmpegui.ui.screens.FileDetailsScreen
import com.leowly.ffmpegui.ui.screens.FileListScreen
import com.leowly.ffmpegui.ui.screens.FileProcessingScreen
import com.leowly.ffmpegui.ui.screens.ModeSelectionScreen
import com.leowly.ffmpegui.ui.screens.SettingsScreen

// --- Root Navigation --- 
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    // A standard Flow must have an initial value for collectAsState.
    // `null` is used to represent the initial loading state before the first value is emitted.
    val userPreferences by userPreferencesRepository.userPreferencesFlow.collectAsState(initial = null)

    // Copy the delegated property to a local variable to enable safe smart casting.
    val currentPreferences = userPreferences

    // This check is crucial for handling the loading state.
    // It is only true while waiting for the first value from DataStore.
    if (currentPreferences == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // After the check, currentPreferences is smart-cast to a non-null type.
    val startDestination = determineStartDestination(currentPreferences)
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = startDestination) {
        composable("selection") {
            ModeSelectionScreen(
                userPreferencesRepository = userPreferencesRepository,
                onLoginSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo("selection") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainAppScreen()
        }
    }
}

private fun determineStartDestination(prefs: UserPreferences): String {
    // If mode is null, it means the user has not made a selection yet.
    if (prefs.mode == null) {
        return "selection"
    }

    // If a mode is set (either "local" or "cloud" with a server address), go to the main screen.
    val hasLocalMode = prefs.mode == "local"
    val hasCloudMode = prefs.mode == "cloud" && prefs.serverAddress.isNotBlank()
    return if (hasLocalMode || hasCloudMode) "main" else "selection"
}

// --- Main App Screen with Bottom Bar --- //

private sealed class BottomNavItem(
    val route: String,
    val titleResId: Int, // Use String Resource ID
    val icon: ImageVector
) {
    object FileList : BottomNavItem("file_list", R.string.bottom_nav_file_list, Icons.AutoMirrored.Filled.List)
    object FileDetails : BottomNavItem("file_details", R.string.bottom_nav_file_details, Icons.Default.Description)
    object FileProcessing : BottomNavItem("file_processing", R.string.bottom_nav_file_processing, Icons.Default.Build)
    object Settings : BottomNavItem("settings", R.string.bottom_nav_settings, Icons.Default.Settings)
}

@Composable
private fun MainAppScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.FileList,
        BottomNavItem.FileDetails,
        BottomNavItem.FileProcessing,
        BottomNavItem.Settings
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.titleResId)) },
                label = { Text(stringResource(id = screen.titleResId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.FileProcessing.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.FileList.route) {
            FileListScreen()
        }
        composable(BottomNavItem.FileDetails.route) {
            FileDetailsScreen()
        }
        composable(BottomNavItem.FileProcessing.route) {
            FileProcessingScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen()
        }
    }
}
