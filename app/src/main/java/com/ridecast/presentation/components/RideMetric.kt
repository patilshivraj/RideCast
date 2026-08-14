package com.ridecast.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ridecast.presentation.theme.RideCastType

/**
 * Label + value pair for ride metrics (temperature, distance, wind, etc.).
 */
@Composable
fun RideMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = value,
            style = if (emphasized) RideCastType.metricPrimary else RideCastType.metricSecondary,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = when (horizontalAlignment) {
                Alignment.CenterHorizontally -> TextAlign.Center
                Alignment.End -> TextAlign.End
                else -> TextAlign.Start
            },
        )
        Text(
            text = label,
            style = RideCastType.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = when (horizontalAlignment) {
                Alignment.CenterHorizontally -> TextAlign.Center
                Alignment.End -> TextAlign.End
                else -> TextAlign.Start
            },
        )
    }
}
