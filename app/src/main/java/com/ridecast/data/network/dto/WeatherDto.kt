package com.ridecast.data.network.dto

import com.google.gson.annotations.SerializedName

data class WeatherForecastResponse(
    val forecast: ForecastDto?,
)

data class ForecastDto(
    val forecastday: List<ForecastDayDto>?,
)

data class ForecastDayDto(
    val hour: List<HourDto>?,
)

data class HourDto(
    @SerializedName("time_epoch") val timeEpoch: Long?,
    @SerializedName("temp_c") val tempC: Double?,
    @SerializedName("feelslike_c") val feelsLikeC: Double?,
    val condition: ConditionDto?,
    @SerializedName("chance_of_rain") val chanceOfRain: Int?,
    @SerializedName("precip_mm") val precipMm: Double?,
    @SerializedName("wind_kph") val windKph: Double?,
    @SerializedName("wind_degree") val windDegree: Int?,
    val humidity: Int?,
    @SerializedName("vis_km") val visKm: Double?,
    val uv: Double?,
)

data class ConditionDto(
    val text: String?,
    val code: Int?,
)
