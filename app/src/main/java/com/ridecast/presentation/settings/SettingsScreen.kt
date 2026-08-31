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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.painterResource
import com.ridecast.R
import com.ridecast.BuildConfig
import com.ridecast.domain.model.DarkMode
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.SamplingConfig
import com.ridecast.domain.model.TemperatureUnit
import com.ridecast.domain.model.WindUnit
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.components.SectionHeader
import com.ridecast.presentation.theme.RideCastSpacing

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { RideCastTopBar("Settings") },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(RideCastSpacing.md),
            verticalArrangement = Arrangement.spacedBy(RideCastSpacing.sm),
        ) {
            item {
                SectionHeader(title = "Route", subtitle = "Weather sampling along your route")
            }
            item {
                SegmentedSettingRow(
                    title = "Sampling distance",
                    options = SamplingConfig.SUPPORTED_INTERVALS.map { "$it km" },
                    selectedIndex = SamplingConfig.SUPPORTED_INTERVALS
                        .indexOf(settings.samplingIntervalKm)
                        .coerceAtLeast(0),
                    onSelected = { idx ->
                        viewModel.setSamplingInterval(SamplingConfig.SUPPORTED_INTERVALS[idx])
                    },
                )
            }

            item { Spacer(Modifier.height(RideCastSpacing.sm)) }

            item { SectionHeader(title = "Units") }
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
                    title = "Wind speed",
                    options = WindUnit.entries.map { it.label },
                    selectedIndex = WindUnit.entries.indexOf(settings.windUnit),
                    onSelected = { viewModel.setWindUnit(WindUnit.entries[it]) },
                )
            }

            item { Spacer(Modifier.height(RideCastSpacing.sm)) }

            item { SectionHeader(title = "Appearance") }
            item {
                SegmentedSettingRow(
                    title = "Dark mode",
                    options = DarkMode.entries.map { it.label },
                    selectedIndex = DarkMode.entries.indexOf(settings.darkMode),
                    onSelected = { viewModel.setDarkMode(DarkMode.entries[it]) },
                )
            }

            item { Spacer(Modifier.height(RideCastSpacing.sm)) }

            item { SectionHeader(title = "Map") }
            item {
                SegmentedSettingRow(
                    title = "Map type",
                    options = MapDisplayType.entries.map { it.label },
                    selectedIndex = MapDisplayType.entries.indexOf(settings.mapType),
                    onSelected = { viewModel.setMapType(MapDisplayType.entries[it]) },
                )
            }

            item { Spacer(Modifier.height(RideCastSpacing.sm)) }

            item { SectionHeader(title = "About") }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ListItem(
                    headlineContent = { Text("RideCast") },
                    supportingContent = { Text("Motorcycle weather route planner") },
                    trailingContent = {
                        Text(
                            "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val uriHandler = LocalUriHandler.current
                ListItem(
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://github.com/patilshivraj/RideCast")
                    },
                    headlineContent = { Text("Open Source") },
                    supportingContent = { Text("For the love of safe rides... by Shivraj Patil") },
                    trailingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = "Open GitHub",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SegmentedSettingRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    subtitle: String? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(RideCastSpacing.sm))
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
