package com.ridecast.presentation.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridecast.domain.model.WeatherPoint
import com.ridecast.presentation.weather.toEmoji
import com.ridecast.presentation.weather.toWeatherCondition
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherMarkerBottomSheet(
    weatherPoint: WeatherPoint,
    distanceKm: Int,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val condition = weatherPoint.weather.conditionCode.toWeatherCondition()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header: time + location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weatherPoint.routePoint.eta.format(
                            DateTimeFormatter.ofPattern("HH:mm, EEE d MMM")
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (distanceKm == 0) "Departure point" else "$distanceKm km from start",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(text = condition.toEmoji(), fontSize = 40.sp)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = weatherPoint.weather.conditionText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            // Stats grid — 2 columns × 4 rows
            val stats = listOf(
                "🌡️ Temperature" to "${weatherPoint.weather.temperatureCelsius.roundToInt()}°C",
                "🤔 Feels Like"  to "${weatherPoint.weather.feelsLikeCelsius.roundToInt()}°C",
                "💧 Rain Chance" to "${weatherPoint.weather.rainProbabilityPercent}%",
                "🌧️ Precip"      to "${weatherPoint.weather.rainAmountMm} mm",
                "💨 Wind"        to "${weatherPoint.weather.windSpeedKph.roundToInt()} km/h",
                "💦 Humidity"    to "${weatherPoint.weather.humidityPercent}%",
                "👁️ Visibility"  to "${weatherPoint.weather.visibilityKm} km",
                "☀️ UV Index"    to "${weatherPoint.weather.uvIndex.roundToInt()}",
            )

            stats.chunked(2).forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { (label, value) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
