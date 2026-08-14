package com.ridecast.presentation.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.ridecast.presentation.components.RideMetric
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.theme.RideCastType
import com.ridecast.presentation.theme.toSemanticColor
import com.ridecast.presentation.weather.WeatherCondition
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun AnimatedTimelineCard(item: TimelineItem, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(item.index) {
        delay(item.index * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) +
            slideInVertically(animationSpec = tween(250)) { it / 3 },
    ) {
        WeatherTimelineCard(item = item, modifier = modifier)
    }
}

@Composable
fun WeatherTimelineCard(
    item: TimelineItem,
    modifier: Modifier = Modifier,
) {
    val accentColor = item.condition.toSemanticColor()
    val isNoteworthy = item.condition in listOf(
        WeatherCondition.RAIN,
        WeatherCondition.HEAVY_RAIN,
        WeatherCondition.STORM,
    ) || item.windSpeedKph > 40 || item.rainProbabilityPercent > 50

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isNoteworthy) 2.dp else 1.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accentColor.copy(alpha = 0.55f)),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(RideCastSpacing.md),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.timeFormatted,
                            style = RideCastType.metricSecondary,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (!item.isOrigin && !item.isDestination) {
                            Spacer(Modifier.width(RideCastSpacing.sm))
                            Text(
                                text = "${item.distanceKm} km",
                                style = RideCastType.caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = item.locationLabel,
                        style = RideCastType.cardTitle,
                        fontWeight = if (item.isOrigin || item.isDestination) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(text = item.conditionEmoji, fontSize = 28.sp)
            }

            if (isNoteworthy) {
                Spacer(Modifier.height(RideCastSpacing.sm))
                Text(
                    text = buildAlertText(item),
                    style = RideCastType.caption,
                    color = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            accentColor.copy(alpha = 0.1f),
                            MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = RideCastSpacing.sm, vertical = RideCastSpacing.xs),
                )
            }

            Spacer(Modifier.height(RideCastSpacing.sm))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = accentColor.copy(alpha = 0.2f),
            )
            Spacer(Modifier.height(RideCastSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RideMetric(
                    label = "Temp",
                    value = "${item.temperatureCelsius.roundToInt()}°",
                    emphasized = true,
                )
                RideMetric(
                    label = "Feels",
                    value = "${item.feelsLikeCelsius.roundToInt()}°",
                )
                RideMetric(
                    label = "Rain",
                    value = "${item.rainProbabilityPercent}%",
                    emphasized = item.rainProbabilityPercent > 50,
                )
                RideMetric(
                    label = "Wind",
                    value = "${item.windSpeedKph.roundToInt()}",
                    emphasized = item.windSpeedKph > 40,
                )
            }

            if (!item.isOrigin && !item.isDestination) {
                Spacer(Modifier.height(RideCastSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Vis ${item.visibilityKm}km · ${item.humidityPercent}% humidity",
                        style = RideCastType.caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            }
        }
    }
}

private fun buildAlertText(item: TimelineItem): String = when {
    item.condition == WeatherCondition.STORM -> "Storm expected — consider delaying"
    item.condition == WeatherCondition.HEAVY_RAIN -> "Heavy rain likely"
    item.rainProbabilityPercent > 50 -> "Rain likely (${item.rainProbabilityPercent}%)"
    item.windSpeedKph > 40 -> "Strong wind (${item.windSpeedKph.roundToInt()} km/h)"
    else -> item.conditionText
}
