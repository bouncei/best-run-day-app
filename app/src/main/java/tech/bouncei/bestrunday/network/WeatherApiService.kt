package tech.bouncei.bestrunday.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import tech.bouncei.bestrunday.data.WeatherResponse

interface WeatherApiService {
    @GET("forecast.json")
    suspend fun getWeatherForecast(
            @Query("key") apiKey: String,
            @Query("q") location: String,
            @Query("days") days: Int = 7,
            @Query("aqi") aqi: String = "no",
            @Query("alerts") alerts: String = "no"
    ): Response<WeatherResponse>
}

object WeatherApi {
    private const val BASE_URL = "https://api.weatherapi.com/v1/"
    // You'll need to get a free API key from https://www.weatherapi.com/
    const val API_KEY = "your_api_key_here" // Replace with your actual API key

    val retrofitService: WeatherApiService by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
    }
}
