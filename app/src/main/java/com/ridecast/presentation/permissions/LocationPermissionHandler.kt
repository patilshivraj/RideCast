package com.ridecast.presentation.permissions

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Handles the full runtime permission flow for location access.
 *
 * - Requests [ACCESS_FINE_LOCATION] and [ACCESS_COARSE_LOCATION].
 * - Shows a rationale [AlertDialog] when the system indicates the user should be given a reason.
 * - Calls [onGranted] and renders [content] when at least coarse location is granted.
 * - Calls [onDenied] when the user has permanently denied both permissions.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    content: @Composable () -> Unit,
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )

    when {
        permissionsState.allPermissionsGranted -> {
            LaunchedEffect(Unit) { onGranted() }
            content()
        }

        permissionsState.shouldShowRationale -> {
            AlertDialog(
                onDismissRequest = { /* non-dismissible — user must choose */ },
                title = { Text("Location Access") },
                text = {
                    Text(
                        "RideCast uses your location to auto-fill your starting point. " +
                            "You can still enter an origin manually without granting access.",
                    )
                },
                confirmButton = {
                    TextButton(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                        Text("Grant")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDenied) {
                        Text("Skip")
                    }
                },
            )
        }

        else -> {
            // Permissions not yet requested — launch request on first composition,
            // or permanently denied — notify caller and still render content so the
            // user can enter locations manually.
            LaunchedEffect(Unit) {
                if (!permissionsState.shouldShowRationale &&
                    permissionsState.permissions.all { !it.status.isGranted }
                ) {
                    permissionsState.launchMultiplePermissionRequest()
                } else {
                    onDenied()
                }
            }
            content()
        }
    }
}
