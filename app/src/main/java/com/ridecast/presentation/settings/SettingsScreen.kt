package com.ridecast.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridecast.domain.model.DarkMode
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.SamplingConfig
import com.ridecast.domain.model.TemperatureUnit
import com.ridecast.domain.model.WindUnit
import com.ridecast.presentation.components.RideCastTopBar

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { RideCastTopBar("Settings") },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SettingsSectionHeader("Route") }
            item {
                SegmentedSettingRow(
                    title = "Sampling Distance",
                    subtitle = "Weather point every N km",
                    options = SamplingConfig.SUPPORTED_INTERVALS.map { "$it km" },
                    selectedIndex = SamplingConfig.SUPPORTED_INTERVALS
                        .indexOf(settings.samplingIntervalKm)
                        .coerceAtLeast(0),
                    onSelected = { idx ->
                        viewModel.setSamplingInterval(SamplingConfig.SUPPORTED_INTERVALS[idx])
                    },
                )
            }

            item { SettingsSectionHeader("Units") }
            item {
                SegmentedSettingRow(
                    title = "Temperature",
                    options = TemperatureUnit.entries.map { it.label },
                    selectedIndex = TemperatureUnit.entries.indexOf(settings.temperatureUnit),
                    onSelected = { viewModel.setTemperatureUnit(TemperatureUnit.entries[it]) },
                )
            }
            item {
                SegmentedSettingRow(
                    title = "Wind Speed",
                    options = WindUnit.entries.map { it.label },
                    selectedIndex = WindUnit.entries.indexOf(settings.windUnit),
                    onSelected = { viewModel.setWindUnit(WindUnit.entries[it]) },
                )
            }

            item { SettingsSectionHeader("Appearance") }
            item {
                SegmentedSettingRow(
                    title = "Dark Mode",
                    options = DarkMode.entries.map { it.label },
                    selectedIndex = DarkMode.entries.indexOf(settings.darkMode),
                    onSelected = { viewModel.setDarkMode(DarkMode.entries[it]) },
                )
            }

            item { SettingsSectionHeader("Map") }
            item {
                SegmentedSettingRow(
                    title = "Map Type",
                    options = MapDisplayType.entries.map { it.label },
                    selectedIndex = MapDisplayType.entries.indexOf(settings.mapType),
                    onSelected = { viewModel.setMapType(MapDisplayType.entries[it]) },
                )
            }

            item { SettingsSectionHeader("About") }
            item {
                ListItem(
                    headlineContent = { Text("RideCast") },
                    supportingContent = { Text("Motorcycle weather route planner") },
                    trailingContent = {
                        Text("v1.0.0", style = MaterialTheme.typography.bodySmall)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun SegmentedSettingRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    subtitle: String? = null,
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = index == selectedIndex,
                        onClick = { onSelected(index) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size,
                        ),
                        label = { Text(label, maxLines = 1) },
                    )
                }
            }
        }
    }
}
