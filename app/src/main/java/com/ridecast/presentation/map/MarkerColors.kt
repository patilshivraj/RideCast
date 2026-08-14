package com.ridecast.presentation.map

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.ridecast.presentation.weather.WeatherCondition

/** Maps a [WeatherCondition] to a Google Maps marker hue (0–360). */
fun WeatherCondition.toMarkerHue(): Float = when (this) {
    WeatherCondition.SUNNY         -> BitmapDescriptorFactory.HUE_GREEN     // 120
    WeatherCondition.PARTLY_CLOUDY -> 80f  // yellow-green
    WeatherCondition.CLOUDY        -> BitmapDescriptorFactory.HUE_YELLOW    // 60
    WeatherCondition.FOG           -> BitmapDescriptorFactory.HUE_ORANGE    // 30
    WeatherCondition.RAIN          -> BitmapDescriptorFactory.HUE_AZURE     // 210
    WeatherCondition.HEAVY_RAIN    -> BitmapDescriptorFactory.HUE_BLUE      // 240
    WeatherCondition.STORM         -> BitmapDescriptorFactory.HUE_RED       // 0
    WeatherCondition.SNOW          -> BitmapDescriptorFactory.HUE_CYAN      // 180
    WeatherCondition.UNKNOWN       -> BitmapDescriptorFactory.HUE_VIOLET    // 270
}
