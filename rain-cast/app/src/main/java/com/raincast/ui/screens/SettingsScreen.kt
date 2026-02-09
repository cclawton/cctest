package com.raincast.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.*
import com.raincast.di.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object PrefsKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val ALERT_LEAD_TIME = intPreferencesKey("alert_lead_time")
    val MIN_INTENSITY = intPreferencesKey("min_intensity")
    val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
    val QUIET_START = intPreferencesKey("quiet_start_hour")
    val QUIET_END = intPreferencesKey("quiet_end_hour")
    val DAILY_FORECAST = booleanPreferencesKey("daily_forecast_enabled")
    val DAILY_FORECAST_HOUR = intPreferencesKey("daily_forecast_hour")
    val ANIMATION_SPEED = longPreferencesKey("animation_speed_ms")
    val RADAR_OPACITY = floatPreferencesKey("radar_opacity")
    val THEME_MODE = intPreferencesKey("theme_mode")
    val AUTO_LOCATION = booleanPreferencesKey("auto_location")
    val DATA_SAVER = booleanPreferencesKey("data_saver")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = context.dataStore

    var notificationsEnabled by remember { mutableStateOf(true) }
    var alertLeadTime by remember { mutableIntStateOf(30) }
    var minIntensity by remember { mutableIntStateOf(0) }
    var quietHoursEnabled by remember { mutableStateOf(true) }
    var dailyForecast by remember { mutableStateOf(false) }
    var autoLocation by remember { mutableStateOf(true) }
    var dataSaver by remember { mutableStateOf(false) }
    var radarOpacity by remember { mutableFloatStateOf(0.7f) }

    LaunchedEffect(Unit) {
        val prefs = dataStore.data.first()
        notificationsEnabled = prefs[PrefsKeys.NOTIFICATIONS_ENABLED] ?: true
        alertLeadTime = prefs[PrefsKeys.ALERT_LEAD_TIME] ?: 30
        minIntensity = prefs[PrefsKeys.MIN_INTENSITY] ?: 0
        quietHoursEnabled = prefs[PrefsKeys.QUIET_HOURS_ENABLED] ?: true
        dailyForecast = prefs[PrefsKeys.DAILY_FORECAST] ?: false
        autoLocation = prefs[PrefsKeys.AUTO_LOCATION] ?: true
        dataSaver = prefs[PrefsKeys.DATA_SAVER] ?: false
        radarOpacity = prefs[PrefsKeys.RADAR_OPACITY] ?: 0.7f
    }

    fun <T> savePref(key: Preferences.Key<T>, value: T) {
        scope.launch {
            dataStore.edit { it[key] = value }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Location section
            SettingsSection("Location") {
                SwitchSetting(
                    title = "Auto-detect location",
                    subtitle = "Use GPS for current position",
                    checked = autoLocation,
                    onCheckedChange = {
                        autoLocation = it
                        savePref(PrefsKeys.AUTO_LOCATION, it)
                    }
                )
            }

            // Notifications section
            SettingsSection("Notifications") {
                SwitchSetting(
                    title = "Rain alerts",
                    subtitle = "Notify when rain is approaching",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        savePref(PrefsKeys.NOTIFICATIONS_ENABLED, it)
                    }
                )
                if (notificationsEnabled) {
                    ListSetting(
                        title = "Alert lead time",
                        value = "$alertLeadTime minutes",
                        options = listOf(15, 30, 45, 60),
                        optionLabels = listOf("15 min", "30 min", "45 min", "60 min"),
                        onSelected = {
                            alertLeadTime = it
                            savePref(PrefsKeys.ALERT_LEAD_TIME, it)
                        }
                    )
                    ListSetting(
                        title = "Minimum intensity",
                        value = when (minIntensity) {
                            0 -> "Any rain"
                            1 -> "Moderate+"
                            2 -> "Heavy+"
                            3 -> "Extreme only"
                            else -> "Any rain"
                        },
                        options = listOf(0, 1, 2, 3),
                        optionLabels = listOf("Any rain", "Moderate+", "Heavy+", "Extreme only"),
                        onSelected = {
                            minIntensity = it
                            savePref(PrefsKeys.MIN_INTENSITY, it)
                        }
                    )
                    SwitchSetting(
                        title = "Quiet hours",
                        subtitle = "No alerts 10 PM - 6 AM",
                        checked = quietHoursEnabled,
                        onCheckedChange = {
                            quietHoursEnabled = it
                            savePref(PrefsKeys.QUIET_HOURS_ENABLED, it)
                        }
                    )
                }
                SwitchSetting(
                    title = "Daily forecast",
                    subtitle = "Morning rain outlook at 7 AM",
                    checked = dailyForecast,
                    onCheckedChange = {
                        dailyForecast = it
                        savePref(PrefsKeys.DAILY_FORECAST, it)
                    }
                )
            }

            // Radar section
            SettingsSection("Radar") {
                SliderSetting(
                    title = "Radar opacity",
                    value = radarOpacity,
                    onValueChange = {
                        radarOpacity = it
                        savePref(PrefsKeys.RADAR_OPACITY, it)
                    }
                )
            }

            // Data section
            SettingsSection("Data") {
                SwitchSetting(
                    title = "Data saver",
                    subtitle = "Reduce radar frames and refresh frequency",
                    checked = dataSaver,
                    onCheckedChange = {
                        dataSaver = it
                        savePref(PrefsKeys.DATA_SAVER, it)
                    }
                )
            }

            // About section
            SettingsSection("About") {
                InfoItem("Version", "1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Radar data \u00A9 RainViewer / Source: Australian Bureau of Meteorology",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Weather data provided by Open-Meteo.com (CC BY 4.0)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "\u00A9 OpenStreetMap contributors",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun <T> ListSetting(
    title: String,
    value: String,
    options: List<T>,
    optionLabels: List<String>,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(value)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(optionLabels[index]) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.1f..1.0f,
            steps = 8
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
