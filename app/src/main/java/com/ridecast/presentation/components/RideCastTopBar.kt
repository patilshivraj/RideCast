package com.ridecast.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Shared top app bar used across all RideCast screens.
 *
 * Wraps Material 3 [TopAppBar] with consistent styling — surface colour, bold title,
 * and support for scroll-aware behaviour (e.g. collapsing on scroll in Milestone 6).
 *
 * @param title          Screen title text.
 * @param modifier       Applied to the bar container.
 * @param navigationIcon Optional back/menu icon slot.
 * @param actions        Optional icon buttons on the trailing end.
 * @param scrollBehavior Optional [TopAppBarScrollBehavior] for collapsing behaviour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideCastTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = navigationIcon,
        actions        = actions,
        scrollBehavior = scrollBehavior,
        modifier       = modifier,
        colors         = TopAppBarDefaults.topAppBarColors(
            containerColor         = MaterialTheme.colorScheme.surface,
            titleContentColor      = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
