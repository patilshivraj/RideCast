package com.ridecast.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideWeather
import com.ridecast.domain.model.Route
import com.ridecast.domain.model.RoutePoint
import com.ridecast.domain.usecase.GetRideWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getRideWeatherUseCase: GetRideWeatherUseCase,
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Result<RideWeather>>(Result.Loading)
    val weatherState: StateFlow<Result<RideWeather>> = _weatherState.asStateFlow()

    fun fetchWeather(route: Route, samplePoints: List<RoutePoint>) {
        if (samplePoints.isEmpty()) return
        viewModelScope.launch {
            getRideWeatherUseCase(route, samplePoints).collect { result ->
                _weatherState.value = result
            }
        }
    }

    fun clearWeather() {
        _weatherState.value = Result.Loading
    }
}
