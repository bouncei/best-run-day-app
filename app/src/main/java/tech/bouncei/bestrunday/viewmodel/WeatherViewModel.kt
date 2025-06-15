package tech.bouncei.bestrunday.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.bouncei.bestrunday.data.WeatherUIModel
import tech.bouncei.bestrunday.repository.WeatherRepository

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()
    
    var uiState by mutableStateOf(WeatherUiState())
        private set
    
    fun loadWeatherData(location: String = "auto:ip") {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            repository.getWeatherForecast7Days(location)
                .onSuccess { weatherData ->
                    uiState = uiState.copy(
                        isLoading = false,
                        weatherData = weatherData,
                        error = null
                    )
                }
                .onFailure { exception ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }
    
    fun refreshWeatherData(location: String = "auto:ip") {
        loadWeatherData(location)
    }
    
    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherUIModel? = null,
    val error: String? = null
) 