package com.ridecast.presentation.map

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.sp
import com.ridecast.domain.model.WeatherPoint
import com.ridecast.presentation.components.RideMetric
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.theme.RideCastType
import com.ridecast.presentation.theme.toSemanticColor
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
    val accentColor = condition.toSemanticColor()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RideCastSpacing.lg)
                .padding(bottom = RideCastSpacing.xl),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weatherPoint.routePoint.eta.format(
                            DateTimeFormatter.ofPattern("HH:mm, EEE d MMM"),
                        ),
                        style = RideCastType.screenTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (distanceKm == 0) "Departure point" else "$distanceKm km from start",
                        style = RideCastType.caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(text = condition.toEmoji(), fontSize = 40.sp)
            }

            Spacer(Modifier.height(RideCastSpacing.xs))
            Text(
                text = weatherPoint.weather.conditionText,
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(RideCastSpacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(RideCastSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RideMetric(
                    label = "Temperature",
                    value = "${weatherPoint.weather.temperatureCelsius.roundToInt()}°C",
                    emphasized = true,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                RideMetric(
                    label = "Feels like",
                    value = "${weatherPoint.weather.feelsLikeCelsius.roundToInt()}°C",
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                RideMetric(
                    label = "Rain chance",
                    value = "${weatherPoint.weather.rainProbabilityPercent}%",
                    emphasized = weatherPoint.weather.rainProbabilityPercent > 50,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }

            Spacer(Modifier.height(RideCastSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RideMetric(
                    label = "Wind",
                    value = "${weatherPoint.weather.windSpeedKph.roundToInt()} km/h",
                    emphasized = weatherPoint.weather.windSpeedKph > 40,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                RideMetric(
                    label = "Humidity",
                    value = "${weatherPoint.weather.humidityPercent}%",
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                RideMetric(
                    label = "Visibility",
                    value = "${weatherPoint.weather.visibilityKm} km",
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }

            Spacer(Modifier.height(RideCastSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RideMetric(
                    label = "Precipitation",
                    value = "${weatherPoint.weather.rainAmountMm} mm",
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                RideMetric(
                    label = "UV index",
                    value = "${weatherPoint.weather.uvIndex.roundToInt()}",
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
        }
    }
}
