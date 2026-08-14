package com.ridecast.core.util

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for monitoring network connectivity.
 *
 * A concrete implementation will be provided in the data layer using
 * [android.net.ConnectivityManager]. Keeping this interface in the core layer means
 * domain use cases can depend on it without touching Android APIs directly.
 */
interface NetworkMonitor {
    /** Emits `true` when the device has an active network connection, `false` otherwise. */
    val isOnline: Flow<Boolean>
}
