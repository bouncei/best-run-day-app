package tech.bouncei.bestrunday.data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current")
    val current: CurrentWeather,
    @SerializedName("forecast")
    val forecast: Forecast
)

data class CurrentWeather(
    @SerializedName("temp_c")
    val tempC: Double,
    @SerializedName("temp_f")
    val tempF: Double,
    @SerializedName("condition")
    val condition: WeatherCondition,
    @SerializedName("wind_kph")
    val windKph: Double,
    @SerializedName("wind_mph")
    val windMph: Double,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("feelslike_c")
    val feelsLikeC: Double,
    @SerializedName("feelslike_f")
    val feelsLikeF: Double,
    @SerializedName("uv")
    val uvIndex: Double
)

data class Forecast(
    @SerializedName("forecastday")
    val forecastDays: List<ForecastDay>
)

data class ForecastDay(
    @SerializedName("date")
    val date: String,
    @SerializedName("day")
    val day: DayWeather,
    @SerializedName("hour")
    val hours: List<HourWeather>
)

data class DayWeather(
    @SerializedName("maxtemp_c")
    val maxTempC: Double,
    @SerializedName("maxtemp_f")
    val maxTempF: Double,
    @SerializedName("mintemp_c")
    val minTempC: Double,
    @SerializedName("mintemp_f")
    val minTempF: Double,
    @SerializedName("condition")
    val condition: WeatherCondition,
    @SerializedName("maxwind_kph")
    val maxWindKph: Double,
    @SerializedName("maxwind_mph")
    val maxWindMph: Double,
    @SerializedName("avghumidity")
    val avgHumidity: Int,
    @SerializedName("daily_chance_of_rain")
    val chanceOfRain: Int,
    @SerializedName("uv")
    val uvIndex: Double
)

data class HourWeather(
    @SerializedName("time")
    val time: String,
    @SerializedName("temp_c")
    val tempC: Double,
    @SerializedName("temp_f")
    val tempF: Double,
    @SerializedName("condition")
    val condition: WeatherCondition,
    @SerializedName("chance_of_rain")
    val chanceOfRain: Int
)

data class WeatherCondition(
    @SerializedName("text")
    val text: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("code")
    val code: Int
)

// UI Model for simplified display
data class WeatherUIModel(
    val currentTemp: String,
    val condition: String,
    val feelsLike: String,
    val windSpeed: String,
    val humidity: String,
    val uvIndex: String,
    val dailyForecasts: List<DailyForecastUI>
)

data class DailyForecastUI(
    val date: String,
    val dayOfWeek: String,
    val condition: String,
    val highTemp: String,
    val lowTemp: String,
    val chanceOfRain: String,
    val windSpeed: String,
    val runningScore: Int // Score from 1-10 for running conditions
) 