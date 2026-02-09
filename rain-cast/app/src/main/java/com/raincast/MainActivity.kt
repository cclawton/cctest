package com.raincast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raincast.ui.screens.*
import com.raincast.ui.theme.RainCastTheme
import com.raincast.workers.RadarSyncWorker

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule background sync
        RadarSyncWorker.schedule(this)

        setContent {
            RainCastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RainCastNavigation()
                }
            }
        }
    }
}

@Composable
fun RainCastNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToFullscreenRadar = { navController.navigate("radar_full") },
                onNavigateToLocationSearch = { navController.navigate("location_search") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("radar_full") {
            RadarFullScreen(
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("location_search") {
            LocationSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onLocationSelected = { lat, lon ->
                    mainViewModel.setLocation(lat, lon)
                }
            )
        }
    }
}
