package tech.bouncei.bestrunday.repository

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import tech.bouncei.bestrunday.data.DailyForecastUI
import tech.bouncei.bestrunday.data.WeatherResponse
import tech.bouncei.bestrunday.data.WeatherUIModel
import tech.bouncei.bestrunday.network.WeatherApi

class WeatherRepository {
        private val apiService = WeatherApi.retrofitService

        suspend fun getWeatherForecast7Days(location: String): Result<WeatherUIModel> {
                // Check if API key is configured
                if (WeatherApi.API_KEY == "your_api_key_here") {
                        return Result.success(getDemoWeatherData())
                }

                return try {
                        val response =
                                apiService.getWeatherForecast(
                                        apiKey = WeatherApi.API_KEY,
                                        location = location,
                                        days = 7
                                )

                        if (response.isSuccessful && response.body() != null) {
                                val weatherResponse = response.body()!!
                                val uiModel = mapToUIModel(weatherResponse)
                                Result.success(uiModel)
                        } else {
                                Result.failure(
                                        Exception(
                                                "Failed to fetch weather data: ${response.message()}"
                                        )
                                )
                        }
                } catch (e: Exception) {
                        Result.failure(e)
                }
        }

        private fun getDemoWeatherData(): WeatherUIModel {
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val demoForecasts = mutableListOf<DailyForecastUI>()
                val dayNames =
                        arrayOf(
                                "Today",
                                "Tomorrow",
                                "Wednesday",
                                "Thursday",
                                "Friday",
                                "Saturday",
                                "Sunday"
                        )
                val conditions =
                        arrayOf(
                                "Sunny",
                                "Partly Cloudy",
                                "Cloudy",
                                "Light Rain",
                                "Sunny",
                                "Clear",
                                "Partly Cloudy"
                        )
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
                                        runningScore =
                                                calculateRunningScore(
                                                        temp =
                                                                highTemps[i % highTemps.size]
                                                                        .toDouble(),
                                                        windSpeed =
                                                                windSpeeds[i % windSpeeds.size]
                                                                        .toDouble(),
                                                        chanceOfRain =
                                                                rainChances[i % rainChances.size],
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
                val dailyForecasts =
                        response.forecast.forecastDays.map { forecastDay ->
                                DailyForecastUI(
                                        date = forecastDay.date,
                                        dayOfWeek = formatDateToDayOfWeek(forecastDay.date),
                                        condition = forecastDay.day.condition.text,
                                        highTemp = "${forecastDay.day.maxTempC.roundToInt()}°C",
                                        lowTemp = "${forecastDay.day.minTempC.roundToInt()}°C",
                                        chanceOfRain = "${forecastDay.day.chanceOfRain}%",
                                        windSpeed =
                                                "${forecastDay.day.maxWindKph.roundToInt()} km/h",
                                        runningScore =
                                                calculateRunningScore(
                                                        temp =
                                                                (forecastDay.day.maxTempC +
                                                                        forecastDay.day.minTempC) /
                                                                        2,
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

        // Calculate running score based on weather conditions (0-100%, 100% being perfect)
        private fun calculateRunningScore(
                temp: Double,
                windSpeed: Double,
                chanceOfRain: Int,
                humidity: Int,
                uvIndex: Double
        ): Int {
                var totalScore = 0.0
                var maxPossibleScore = 0.0

                // Temperature scoring (ideal: 15-22°C) - Weight: 30%
                val tempWeight = 30.0
                val tempScore =
                        when {
                                temp in 15.0..22.0 -> 100.0 // Perfect temperature
                                temp in 10.0..14.9 || temp in 22.1..25.0 -> 80.0 // Good
                                temp in 5.0..9.9 || temp in 25.1..28.0 -> 60.0 // Fair
                                temp in 0.0..4.9 || temp in 28.1..32.0 -> 30.0 // Poor
                                else -> 10.0 // Very poor (below 0°C or above 32°C)
                        }
                totalScore += (tempScore * tempWeight / 100)
                maxPossibleScore += tempWeight

                // Rain chance scoring - Weight: 25%
                val rainWeight = 25.0
                val rainScore =
                        when {
                                chanceOfRain <= 10 -> 100.0 // Perfect (minimal rain chance)
                                chanceOfRain <= 20 -> 85.0 // Excellent
                                chanceOfRain <= 30 -> 70.0 // Good
                                chanceOfRain <= 50 -> 50.0 // Fair
                                chanceOfRain <= 70 -> 30.0 // Poor
                                else -> 10.0 // Very poor (high rain chance)
                        }
                totalScore += (rainScore * rainWeight / 100)
                maxPossibleScore += rainWeight

                // Wind speed scoring (ideal: < 15 km/h) - Weight: 20%
                val windWeight = 20.0
                val windScore =
                        when {
                                windSpeed <= 10 -> 100.0 // Perfect (calm)
                                windSpeed <= 15 -> 90.0 // Excellent
                                windSpeed <= 20 -> 75.0 // Good
                                windSpeed <= 25 -> 60.0 // Fair
                                windSpeed <= 35 -> 40.0 // Poor
                                else -> 20.0 // Very poor (very windy)
                        }
                totalScore += (windScore * windWeight / 100)
                maxPossibleScore += windWeight

                // Humidity scoring (ideal: 40-60%) - Weight: 15%
                val humidityWeight = 15.0
                val humidityScore =
                        when {
                                humidity in 40..60 -> 100.0 // Perfect
                                humidity in 30..39 || humidity in 61..70 -> 80.0 // Good
                                humidity in 20..29 || humidity in 71..80 -> 60.0 // Fair
                                humidity in 10..19 || humidity in 81..90 -> 40.0 // Poor
                                else -> 20.0 // Very poor (too dry or too humid)
                        }
                totalScore += (humidityScore * humidityWeight / 100)
                maxPossibleScore += humidityWeight

                // UV index scoring (moderate UV is okay for running) - Weight: 10%
                val uvWeight = 10.0
                val uvScore =
                        when {
                                uvIndex <= 3 -> 100.0 // Perfect (low UV)
                                uvIndex <= 5 -> 90.0 // Excellent (moderate)
                                uvIndex <= 7 -> 75.0 // Good
                                uvIndex <= 9 -> 60.0 // Fair (high)
                                uvIndex <= 11 -> 40.0 // Poor (very high)
                                else -> 20.0 // Very poor (extreme)
                        }
                totalScore += (uvScore * uvWeight / 100)
                maxPossibleScore += uvWeight

                // Calculate final percentage
                val finalPercentage = ((totalScore / maxPossibleScore) * 100).toInt()
                return maxOf(0, minOf(100, finalPercentage)) // Ensure it's between 0-100
        }
}
