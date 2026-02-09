package com.raincast.ui.screens

import android.location.Address
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.raincast.RainCastApp
import com.raincast.data.local.entities.SavedLocationEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    onNavigateBack: () -> Unit = {},
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val app = context.applicationContext as RainCastApp
    val geocodeHelper = app.appModule.geocodeHelper
    val locationRepo = app.appModule.locationRepository
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val savedLocations by locationRepo.savedLocations.collectAsState(initial = emptyList())
    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingSaveLat by remember { mutableDoubleStateOf(0.0) }
    var pendingSaveLon by remember { mutableDoubleStateOf(0.0) }
    var pendingSaveName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search for a location...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                        }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true
            )

            // Search button
            Button(
                onClick = {
                    scope.launch {
                        isSearching = true
                        searchResults = geocodeHelper.searchLocations(searchQuery)
                        isSearching = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = searchQuery.isNotEmpty() && !isSearching
            ) {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                // Search results
                if (searchResults.isNotEmpty()) {
                    item {
                        Text(
                            "Search Results",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(searchResults) { address ->
                        val name = address.getAddressLine(0) ?: "${address.latitude}, ${address.longitude}"
                        ListItem(
                            headlineContent = { Text(name, maxLines = 2) },
                            leadingContent = { Icon(Icons.Default.LocationOn, null) },
                            trailingContent = {
                                IconButton(onClick = {
                                    pendingSaveLat = address.latitude
                                    pendingSaveLon = address.longitude
                                    pendingSaveName = address.locality ?: address.getAddressLine(0) ?: ""
                                    showSaveDialog = true
                                }) {
                                    Icon(Icons.Default.BookmarkAdd, "Save")
                                }
                            },
                            modifier = Modifier.clickable {
                                onLocationSelected(address.latitude, address.longitude)
                                onNavigateBack()
                            }
                        )
                    }
                }

                // Saved locations
                if (savedLocations.isNotEmpty()) {
                    item {
                        Text(
                            "Saved Locations",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(savedLocations) { location ->
                        ListItem(
                            headlineContent = { Text(location.label) },
                            supportingContent = {
                                Text("%.4f, %.4f".format(location.latitude, location.longitude))
                            },
                            leadingContent = { Icon(Icons.Default.Bookmark, null) },
                            trailingContent = {
                                IconButton(onClick = {
                                    scope.launch {
                                        locationRepo.deleteLocation(location)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            },
                            modifier = Modifier.clickable {
                                onLocationSelected(location.latitude, location.longitude)
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }
    }

    // Save dialog
    if (showSaveDialog) {
        var label by remember { mutableStateOf(pendingSaveName) }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Location") },
            text = {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        locationRepo.saveLocation(label, pendingSaveLat, pendingSaveLon)
                    }
                    showSaveDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
