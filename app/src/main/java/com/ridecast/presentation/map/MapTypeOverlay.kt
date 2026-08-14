package com.ridecast.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridecast.domain.model.MapDisplayType

private val OverlayMapTypes = listOf(
    MapDisplayType.NORMAL,
    MapDisplayType.SATELLITE,
    MapDisplayType.HYBRID,
    MapDisplayType.TERRAIN,
)

/**
 * Floating map-type control (Google Maps–style layers button).
 * Persists selection via [onMapTypeSelected] so Settings and map stay in sync.
 */
@Composable
fun MapTypeOverlay(
    mapType: MapDisplayType,
    onMapTypeSelected: (MapDisplayType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = "Change map type",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            OverlayMapTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    leadingIcon = {
                        if (type == mapType) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        onMapTypeSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}
