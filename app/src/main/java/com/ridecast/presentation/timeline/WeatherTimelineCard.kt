package com.ridecast.presentation.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridecast.presentation.weather.WeatherCondition
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun AnimatedTimelineCard(item: TimelineItem, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(item.index) {
        delay(item.index * 80L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(animationSpec = tween(300)) { it / 2 },
    ) {
        WeatherTimelineCard(item = item, modifier = modifier)
    }
}

@Composable
fun WeatherTimelineCard(
    item: TimelineItem,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (item.condition) {
        WeatherCondition.SUNNY, WeatherCondition.PARTLY_CLOUDY ->
            MaterialTheme.colorScheme.primaryContainer
        WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN ->
            MaterialTheme.colorScheme.secondaryContainer
        WeatherCondition.STORM ->
            MaterialTheme.colorScheme.errorContainer
        WeatherCondition.CLOUDY, WeatherCondition.FOG ->
            MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.timeFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(56.dp),
                )
                Text(
                    text = item.locationLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isOrigin || item.isDestination) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
                Text(text = item.conditionEmoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.conditionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherStat(label = "Temp", value = "${item.temperatureCelsius.roundToInt()}°C")
                WeatherStat(label = "Feels", value = "${item.feelsLikeCelsius.roundToInt()}°C")
                WeatherStat(label = "Rain", value = "${item.rainProbabilityPercent}%")
                WeatherStat(label = "Wind", value = "${item.windSpeedKph.roundToInt()} km/h")
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherStat(label = "Visibility", value = "${item.visibilityKm}km", modifier = Modifier.weight(1f))
                WeatherStat(label = "Precip", value = "${item.rainAmountMm}mm", modifier = Modifier.weight(1f))
                WeatherStat(label = "Humidity", value = "${item.humidityPercent}%", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeatherStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
