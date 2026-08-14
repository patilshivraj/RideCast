package com.ridecast.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridecast.presentation.theme.RideCastSpacing

@Composable
fun ShimmerTimelineList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(
            horizontal = RideCastSpacing.md,
            vertical = RideCastSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(RideCastSpacing.md),
    ) {
        repeat(5) {
            ShimmerBox(height = 100.dp, cornerRadius = RideCastSpacing.md)
        }
    }
}
