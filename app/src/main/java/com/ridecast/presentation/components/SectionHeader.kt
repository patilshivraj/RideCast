package com.ridecast.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.theme.RideCastType

/**
 * Section header with optional subtitle — used to group related controls on a screen.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(modifier = modifier.padding(bottom = RideCastSpacing.sm)) {
        Text(
            text = title.uppercase(),
            style = RideCastType.sectionTitle,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(RideCastSpacing.xs))
            Text(
                text = subtitle,
                style = RideCastType.caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
