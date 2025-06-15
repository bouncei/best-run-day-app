package tech.bouncei.bestrunday.repository

import tech.bouncei.bestrunday.data.DailyForecastUI
import tech.bouncei.bestrunday.data.WeatherResponse
import tech.bouncei.bestrunday.data.WeatherUIModel
import tech.bouncei.bestrunday.network.WeatherApi
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WeatherRepository {
    private val apiService = WeatherApi.retrofitService
    
    suspend fun getWeatherForecast7Days(location: String): Result<WeatherUIModel> {
        // Check if API key is configured
        if (WeatherApi.API_KEY == "your_api_key_here") {
            return Result.success(getDemoWeatherData())
        }
        
        return try {
            val response = apiService.getWeatherForecast(
                apiKey = WeatherApi.API_KEY,
                location = location,
                days = 7
            )
            
            if (response.isSuccessful && response.body() != null) {
                val weatherResponse = response.body()!!
                val uiModel = mapToUIModel(weatherResponse)
                Result.success(uiModel)
            } else {
                Result.failure(Exception("Failed to fetch weather data: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getDemoWeatherData(): WeatherUIModel {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val demoForecasts = mutableListOf<DailyForecastUI>()
        val dayNames = arrayOf("Today", "Tomorrow", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val conditions = arrayOf("Sunny", "Partly Cloudy", "Cloudy", "Light Rain", "Sunny", "Clear", "Partly Cloudy")
        val highTemps = arrayOf(22, 18, 15, 12, 25, 21, 19)
        val lowTemps = arrayOf(12, 8, 5, 7, 15, 11, 9)
        val rainChances = arrayOf(10, 20, 30, 70, 0, 5, 15)
        val windSpeeds = arrayOf(8, 12, 15, 20, 6, 10, 14)
        
        for (i in 0..6) {
            val date = sdf.format(calendar.time)
            val dayName = if (i < dayNames.size) dayNames[i] else "Day ${i + 1}"
            
            demoForecasts.add(
                DailyForecastUI(
                    date = date,
                    dayOfWeek = dayName,
                    condition = conditions[i % conditions.size],
                    highTemp = "${highTemps[i % highTemps.size]}°C",
                    lowTemp = "${lowTemps[i % lowTemps.size]}°C",
                    chanceOfRain = "${rainChances[i % rainChances.size]}%",
                    windSpeed = "${windSpeeds[i % windSpeeds.size]} km/h",
                    runningScore = calculateRunningScore(
                        temp = highTemps[i % highTemps.size].toDouble(),
                        windSpeed = windSpeeds[i % windSpeeds.size].toDouble(),
                        chanceOfRain = rainChances[i % rainChances.size],
                        humidity = 55,
                        uvIndex = 4.0
                    )
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return WeatherUIModel(
            currentTemp = "20°C",
            condition = "Partly Cloudy",
            feelsLike = "Feels like 22°C",
            windSpeed = "10 km/h",
            humidity = "55%",
            uvIndex = "UV 4",
            dailyForecasts = demoForecasts
        )
    }
    
    private fun mapToUIModel(response: WeatherResponse): WeatherUIModel {
        val current = response.current
        val dailyForecasts = response.forecast.forecastDays.map { forecastDay ->
            DailyForecastUI(
                date = forecastDay.date,
                dayOfWeek = formatDateToDayOfWeek(forecastDay.date),
                condition = forecastDay.day.condition.text,
                highTemp = "${forecastDay.day.maxTempC.roundToInt()}°C",
                lowTemp = "${forecastDay.day.minTempC.roundToInt()}°C",
                chanceOfRain = "${forecastDay.day.chanceOfRain}%",
                windSpeed = "${forecastDay.day.maxWindKph.roundToInt()} km/h",
                runningScore = calculateRunningScore(
                    temp = (forecastDay.day.maxTempC + forecastDay.day.minTempC) / 2,
                    windSpeed = forecastDay.day.maxWindKph,
                    chanceOfRain = forecastDay.day.chanceOfRain,
                    humidity = forecastDay.day.avgHumidity,
                    uvIndex = forecastDay.day.uvIndex
                )
            )
        }
        
        return WeatherUIModel(
            currentTemp = "${current.tempC.roundToInt()}°C",
            condition = current.condition.text,
            feelsLike = "Feels like ${current.feelsLikeC.roundToInt()}°C",
            windSpeed = "${current.windKph.roundToInt()} km/h",
            humidity = "${current.humidity}%",
            uvIndex = "UV ${current.uvIndex.roundToInt()}",
            dailyForecasts = dailyForecasts
        )
    }
    
    private fun formatDateToDayOfWeek(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    // Calculate running score based on weather conditions (1-10, 10 being perfect)
    private fun calculateRunningScore(
        temp: Double,
        windSpeed: Double,
        chanceOfRain: Int,
        humidity: Int,
        uvIndex: Double
    ): Int {
        var score = 10
        
        // Temperature scoring (ideal: 15-22°C)
        when {
            temp < 5 || temp > 30 -> score -= 3
            temp < 10 || temp > 25 -> score -= 2
            temp < 15 || temp > 22 -> score -= 1
        }
        
        // Rain chance scoring
        when {
            chanceOfRain > 70 -> score -= 3
            chanceOfRain > 40 -> score -= 2
            chanceOfRain > 20 -> score -= 1
        }
        
        // Wind speed scoring (ideal: < 15 km/h)
        when {
            windSpeed > 30 -> score -= 2
            windSpeed > 20 -> score -= 1
        }
        
        // Humidity scoring (ideal: 40-60%)
        when {
            humidity > 80 || humidity < 30 -> score -= 1
        }
        
        // UV index scoring (avoid very high UV)
        when {
            uvIndex > 8 -> score -= 1
        }
        
        return maxOf(1, score) // Ensure minimum score of 1
    }
} 