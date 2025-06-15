# Best Run Day App 🏃‍♂️

A beautiful Android app that helps runners find the best days to run by providing weather forecasts with running-specific percentage scores.

## Features

- **Current Weather Display**: Shows current temperature, conditions, wind speed, humidity, and UV index
- **7-Day Forecast**: Detailed weather forecast for the next 7 days
- **Running Score**: Each day gets a percentage score from 0-100% based on ideal running conditions
- **Beautiful UI**: Modern Material Design 3 interface with gradient backgrounds and smooth animations
- **Auto Location**: Uses IP-based location detection (can be customized)
- **Refresh Function**: Pull to refresh or tap the refresh button to get updated weather data

## Running Score Algorithm

The app calculates a running score (0-100%) for each day based on weighted factors:

- **Temperature (30% weight)**: Ideal range 15-22°C (59-72°F)
- **Rain Chance (25% weight)**: Lower chance of rain = higher score
- **Wind Speed (20% weight)**: Ideal under 15 km/h (9 mph)
- **Humidity (15% weight)**: Optimal range 40-60%
- **UV Index (10% weight)**: Moderate UV levels preferred

**Score Legend:**

- 🟢 80-100%: Excellent running conditions
- 🌟 60-79%: Very good running conditions
- 🟠 40-59%: Good running conditions
- 🔴 20-39%: Fair running conditions
- ⚫ 0-19%: Poor running conditions

## Setup Instructions

### 1. Get a Weather API Key

1. Visit [WeatherAPI.com](https://www.weatherapi.com/)
2. Sign up for a free account
3. Copy your API key from the dashboard

### 2. Configure the App

1. Open `app/src/main/java/tech/bouncei/bestrunday/network/WeatherApiService.kt`
2. Replace `"your_api_key_here"` with your actual API key:

```kotlin
const val API_KEY = "your_actual_api_key_here"
```

### 3. Build and Run

1. Open the project in Android Studio
2. Sync the project to download dependencies
3. Run the app on a device or emulator

## Technical Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **HTTP Client**: Retrofit + OkHttp
- **JSON Parsing**: Gson
- **State Management**: Compose State + ViewModel
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## Project Structure

```
app/src/main/java/tech/bouncei/bestrunday/
├── data/                    # Data models and DTOs
│   └── WeatherData.kt      # Weather response models
├── network/                 # API service layer
│   └── WeatherApiService.kt # Retrofit service interface
├── repository/              # Data repository
│   └── WeatherRepository.kt # Data operations and mapping
├── viewmodel/              # ViewModels
│   └── WeatherViewModel.kt # UI state management
├── ui/screens/             # Compose UI screens
│   └── WeatherScreen.kt    # Main weather display
└── MainActivity.kt         # Entry point
```

## Permissions

The app requires:

- `INTERNET`: To fetch weather data from the API
- `ACCESS_FINE_LOCATION`: For more accurate location-based weather (optional)
- `ACCESS_COARSE_LOCATION`: For location-based weather (optional)

## API Usage

The app uses the WeatherAPI.com service, which provides:

- Current weather conditions
- 7-day detailed forecasts
- Hourly data
- Weather icons and conditions

Free tier includes:

- 1,000,000 calls per month
- 7-day forecast
- Real-time weather data

## Customization

### Location

By default, the app uses `"auto:ip"` for location detection. You can customize this in the ViewModel:

```kotlin
fun loadWeatherData(location: String = "New York") // Custom city
```

### Temperature Units

The app uses Celsius by default. To switch to Fahrenheit, modify the data mapping in `WeatherRepository.kt`.

### Running Score Parameters

Adjust the scoring algorithm in `WeatherRepository.calculateRunningScore()` to match your running preferences. The current weights are:

- Temperature: 30%
- Rain Chance: 25%
- Wind Speed: 20%
- Humidity: 15%
- UV Index: 10%

## Error Handling

The app includes comprehensive error handling:

- Network connectivity issues
- Invalid API responses
- Missing API key warnings
- User-friendly error messages with retry options

## Future Enhancements

- 🌍 GPS location integration
- 📊 Historical weather data
- ⏰ Best time of day recommendations
- 🏃‍♀️ Personal running preferences
- 📱 Weather notifications
- 🗺️ Multiple location support
- 📈 Running score trends

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is open source and available under the [MIT License](LICENSE).
