package tech.bouncei.bestrunday.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import tech.bouncei.bestrunday.data.DailyForecastUI
import tech.bouncei.bestrunday.data.WeatherUIModel
import tech.bouncei.bestrunday.network.WeatherApi
import tech.bouncei.bestrunday.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
        val uiState = viewModel.uiState
        val hapticFeedback = LocalHapticFeedback.current

        LaunchedEffect(Unit) { viewModel.loadWeatherData() }

        // Auto-refresh every 15 minutes
        LaunchedEffect(Unit) {
                while (true) {
                        delay(15 * 60 * 1000) // 15 minutes
                        if (!uiState.isLoading) {
                                viewModel.refreshWeatherData()
                        }
                }
        }

        Scaffold(
                topBar = {
                        EnhancedTopAppBar(
                                onRefresh = { viewModel.refreshWeatherData() },
                                isRefreshing = uiState.isLoading
                        )
                }
        ) { paddingValues ->
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(
                                                brush =
                                                        Brush.verticalGradient(
                                                                colors =
                                                                        listOf(
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        ),
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .background
                                                                        )
                                                        )
                                        )
                                        .padding(paddingValues)
                ) {
                        AnimatedContent(
                                targetState =
                                        when {
                                                uiState.isLoading && uiState.weatherData == null ->
                                                        "loading"
                                                uiState.error != null &&
                                                        uiState.weatherData == null -> "error"
                                                uiState.weatherData != null -> "content"
                                                else -> "empty"
                                        },
                                transitionSpec = {
                                        slideInVertically { it } + fadeIn() with
                                                slideOutVertically { -it } + fadeOut()
                                },
                                label = "content_transition"
                        ) { state ->
                                when (state) {
                                        "loading" -> EnhancedLoadingContent()
                                        "error" ->
                                                EnhancedErrorContent(
                                                        error = uiState.error ?: "Unknown error",
                                                        onRetry = { viewModel.refreshWeatherData() }
                                                )
                                        "content" -> {
                                                uiState.weatherData?.let { weatherData ->
                                                        EnhancedWeatherContent(
                                                                weatherData = weatherData,
                                                                onScoreClick = { score ->
                                                                        hapticFeedback
                                                                                .performHapticFeedback(
                                                                                        androidx.compose
                                                                                                .ui
                                                                                                .hapticfeedback
                                                                                                .HapticFeedbackType
                                                                                                .LongPress
                                                                                )
                                                                }
                                                        )
                                                }
                                        }
                                        else -> EnhancedEmptyContent()
                                }
                        }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTopAppBar(onRefresh: () -> Unit, isRefreshing: Boolean) {
        val rotation by
                animateFloatAsState(
                        targetValue = if (isRefreshing) 360f else 0f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                ),
                        label = "refresh_rotation"
                )

        TopAppBar(
                title = {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                Text("🏃‍♂️", fontSize = 28.sp)
                                Column {
                                        Text(
                                                "Best Run Day",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                        )
                                        if (WeatherApi.API_KEY == "your_api_key_here") {
                                                Text(
                                                        "Demo Mode",
                                                        fontSize = 12.sp,
                                                        color = Color.White.copy(alpha = 0.7f)
                                                )
                                        }
                                }
                        }
                },
                actions = {
                        IconButton(
                                onClick = onRefresh,
                                modifier = Modifier.rotate(if (isRefreshing) rotation else 0f)
                        ) {
                                Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color.White
                                )
                        }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier =
                        Modifier.background(
                                brush =
                                        Brush.horizontalGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme.primary,
                                                                MaterialTheme.colorScheme.tertiary
                                                        )
                                        )
                        )
        )
}

@Composable
fun EnhancedLoadingContent() {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                        PulsingLoadingIndicator()

                        Text(
                                "Fetching perfect running weather...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                        )

                        // Skeleton cards
                        repeat(3) { index ->
                                AnimatedVisibility(
                                        visible = true,
                                        enter = slideInVertically { it } + fadeIn(),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        LaunchedEffect(Unit) { delay(index * 200L) }
                                        SkeletonCard()
                                }
                        }
                }
        }
}

@Composable
fun SkeletonCard() {
        val shimmerColors =
                listOf(
                        Color.LightGray.copy(alpha = 0.3f),
                        Color.LightGray.copy(alpha = 0.1f),
                        Color.LightGray.copy(alpha = 0.3f)
                )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by
                transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1000f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1200, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Restart
                                ),
                        label = "shimmer_translate"
                )

        val brush =
                Brush.linearGradient(
                        colors = shimmerColors,
                        start =
                                androidx.compose.ui.geometry.Offset(
                                        translateAnim - 300f,
                                        translateAnim - 300f
                                ),
                        end = androidx.compose.ui.geometry.Offset(translateAnim, translateAnim)
                )

        Card(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) { Box(modifier = Modifier.fillMaxSize().background(brush)) }
}

@Composable
fun PulsingLoadingIndicator(modifier: Modifier = Modifier) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsing")

        val scale by
                infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                ),
                        label = "scale"
                )

        val alpha by
                infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                ),
                        label = "alpha"
                )

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                        modifier = Modifier.size(60.dp).scale(scale),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        strokeWidth = 4.dp
                )

                Text("🏃‍♂️", fontSize = 24.sp, modifier = Modifier.scale(scale))
        }
}

@Composable
fun EnhancedErrorContent(error: String, onRetry: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                        // Animated error icon
                        val bounce by
                                rememberInfiniteTransition(label = "error_bounce")
                                        .animateFloat(
                                                initialValue = 0.9f,
                                                targetValue = 1.1f,
                                                animationSpec =
                                                        infiniteRepeatable(
                                                                animation =
                                                                        tween(
                                                                                1000,
                                                                                easing =
                                                                                        FastOutSlowInEasing
                                                                        ),
                                                                repeatMode = RepeatMode.Reverse
                                                        ),
                                                label = "bounce"
                                        )

                        Text("⚠️", fontSize = 64.sp, modifier = Modifier.scale(bounce))

                        Text(
                                "Oops! Weather data unavailable",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                        )

                        Text(
                                error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                        )

                        // Enhanced retry button
                        Button(
                                onClick = onRetry,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                shape = RoundedCornerShape(28.dp)
                        ) {
                                Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Try Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // API key instruction card with better design
                        if (WeatherApi.API_KEY == "your_api_key_here") {
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                Color(0xFF2196F3).copy(alpha = 0.1f)
                                                ),
                                        shape = RoundedCornerShape(16.dp)
                                ) {
                                        Column(
                                                modifier = Modifier.padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                Text(
                                                        "🔑 API Key Required",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2196F3)
                                                )
                                                Text(
                                                        "Get your free API key from weatherapi.com and add it to WeatherApiService.kt to see real weather data!",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Center,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.8f
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
fun EnhancedEmptyContent() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                        "No weather data available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
        }
}

@Composable
fun EnhancedWeatherContent(weatherData: WeatherUIModel, onScoreClick: (Int) -> Unit) {
        LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
        ) {
                // Demo mode banner with better design
                if (WeatherApi.API_KEY == "your_api_key_here") {
                        item { EnhancedDemoModeBanner() }
                }

                // Enhanced current weather card
                item { EnhancedCurrentWeatherCard(weatherData) }

                // Section header
                item {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        "7-Day Running Forecast",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                )

                                // Best day indicator
                                val bestDay =
                                        weatherData.dailyForecasts.maxByOrNull { it.runningScore }
                                bestDay?.let {
                                        Text(
                                                "Best: ${it.dayOfWeek}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }

                // Enhanced daily forecast cards
                items(weatherData.dailyForecasts) { forecast ->
                        EnhancedDailyForecastCard(
                                forecast = forecast,
                                onScoreClick = { onScoreClick(forecast.runningScore) }
                        )
                }

                // Footer spacing
                item { Spacer(modifier = Modifier.height(32.dp)) }
        }
}

@Composable
fun EnhancedDemoModeBanner() {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                        CardDefaults.cardColors(
                                containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        Text("🎭", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        "Demo Mode Active",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9800)
                                )
                                Text(
                                        "Showing sample data • Get your API key at weatherapi.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.7f
                                                )
                                )
                        }
                }
        }
}

@Composable
fun EnhancedCurrentWeatherCard(weatherData: WeatherUIModel) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(20.dp)
        ) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                brush =
                                                        Brush.linearGradient(
                                                                colors =
                                                                        listOf(
                                                                                Color(0xFF6366F1),
                                                                                Color(0xFF8B5CF6),
                                                                                Color(0xFFA855F7)
                                                                        ),
                                                                start =
                                                                        androidx.compose.ui.geometry
                                                                                .Offset(0f, 0f),
                                                                end =
                                                                        androidx.compose.ui.geometry
                                                                                .Offset(
                                                                                        Float.POSITIVE_INFINITY,
                                                                                        Float.POSITIVE_INFINITY
                                                                                )
                                                        )
                                        )
                ) {
                        Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                Text(
                                        "Current Weather",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Weather icon with condition
                                WeatherIcon(condition = weatherData.condition, size = 64)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Temperature with animation
                                val animatedTemp by
                                        animateIntAsState(
                                                targetValue =
                                                        weatherData
                                                                .currentTemp
                                                                .filter { it.isDigit() }
                                                                .toIntOrNull()
                                                                ?: 0,
                                                animationSpec = tween(1000),
                                                label = "temp_animation"
                                        )

                                Text(
                                        "${animatedTemp}°C",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 56.sp
                                )

                                Text(
                                        weatherData.condition,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                )

                                Text(
                                        weatherData.feelsLike,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Weather info grid
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                        EnhancedWeatherInfoItem("💨", "Wind", weatherData.windSpeed)
                                        EnhancedWeatherInfoItem(
                                                "💧",
                                                "Humidity",
                                                weatherData.humidity
                                        )
                                        EnhancedWeatherInfoItem(
                                                "☀️",
                                                "UV Index",
                                                weatherData.uvIndex
                                        )
                                }
                        }
                }
        }
}

@Composable
fun WeatherIcon(condition: String, modifier: Modifier = Modifier, size: Int = 48) {
        val icon =
                when {
                        condition.contains("sunny", ignoreCase = true) -> "☀️"
                        condition.contains("partly cloudy", ignoreCase = true) -> "⛅"
                        condition.contains("cloudy", ignoreCase = true) -> "☁️"
                        condition.contains("rain", ignoreCase = true) -> "🌧️"
                        condition.contains("snow", ignoreCase = true) -> "❄️"
                        condition.contains("storm", ignoreCase = true) -> "⛈️"
                        condition.contains("mist", ignoreCase = true) -> "🌫️"
                        condition.contains("clear", ignoreCase = true) -> "🌙"
                        else -> "🌤️"
                }

        val rotation by
                rememberInfiniteTransition(label = "weather_icon_rotation")
                        .animateFloat(
                                initialValue = 0f,
                                targetValue =
                                        if (condition.contains("sunny", ignoreCase = true)) 360f
                                        else 0f,
                                animationSpec =
                                        infiniteRepeatable(
                                                animation = tween(8000, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                        ),
                                label = "rotation"
                        )

        Text(text = icon, fontSize = size.sp, modifier = modifier.rotate(rotation))
}

@Composable
fun EnhancedWeatherInfoItem(
        icon: String,
        label: String,
        value: String,
        modifier: Modifier = Modifier
) {
        val scale by
                animateFloatAsState(
                        targetValue = 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                ),
                        label = "info_scale"
                )

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.scale(scale)
        ) {
                Text(text = icon, fontSize = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
                Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                )
                Text(
                        value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                )
        }
}

@Composable
fun EnhancedDailyForecastCard(forecast: DailyForecastUI, onScoreClick: () -> Unit) {
        var isExpanded by remember { mutableStateOf(false) }
        val hapticFeedback = LocalHapticFeedback.current

        // Animation states
        val cardElevation by
                animateFloatAsState(
                        targetValue = if (isExpanded) 12f else 6f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "card_elevation"
                )

        val cardScale by
                animateFloatAsState(
                        targetValue = if (isExpanded) 1.02f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "card_scale"
                )

        // Score-based gradient colors
        val scoreGradientColors =
                when {
                        forecast.runningScore >= 80 -> listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                        forecast.runningScore >= 60 -> listOf(Color(0xFF8BC34A), Color(0xFF9CCC65))
                        forecast.runningScore >= 40 -> listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                        forecast.runningScore >= 20 -> listOf(Color(0xFFFF5722), Color(0xFFFF7043))
                        else -> listOf(Color(0xFFf44336), Color(0xFFEF5350))
                }

        Card(
                modifier =
                        Modifier.fillMaxWidth().scale(cardScale).clickable {
                                isExpanded = !isExpanded
                                hapticFeedback.performHapticFeedback(
                                        androidx.compose.ui.hapticfeedback.HapticFeedbackType
                                                .TextHandleMove
                                )
                        },
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
                Column {
                        // Main card content with gradient accent
                        Box {
                                // Subtle gradient accent bar
                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(4.dp)
                                                        .background(
                                                                brush =
                                                                        Brush.horizontalGradient(
                                                                                scoreGradientColors
                                                                        )
                                                        )
                                )

                                Column(modifier = Modifier.padding(24.dp)) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                // Day and condition section
                                                Column(modifier = Modifier.weight(2.5f)) {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(8.dp)
                                                        ) {
                                                                Text(
                                                                        forecast.dayOfWeek,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .headlineSmall,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface
                                                                )

                                                                // Best day indicator
                                                                if (forecast.runningScore >= 80) {
                                                                        Surface(
                                                                                shape =
                                                                                        RoundedCornerShape(
                                                                                                12.dp
                                                                                        ),
                                                                                color =
                                                                                        Color(
                                                                                                        0xFF4CAF50
                                                                                                )
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.15f
                                                                                                )
                                                                        ) {
                                                                                Text(
                                                                                        "BEST",
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        horizontal =
                                                                                                                8.dp,
                                                                                                        vertical =
                                                                                                                4.dp
                                                                                                ),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .labelSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        color =
                                                                                                Color(
                                                                                                        0xFF4CAF50
                                                                                                )
                                                                                )
                                                                        }
                                                                }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(12.dp)
                                                        ) {
                                                                // Enhanced weather icon with
                                                                // background
                                                                Surface(
                                                                        shape = CircleShape,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.3f
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.size(48.dp)
                                                                ) {
                                                                        Box(
                                                                                contentAlignment =
                                                                                        Alignment
                                                                                                .Center,
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
                                                                        ) {
                                                                                WeatherIcon(
                                                                                        condition =
                                                                                                forecast.condition,
                                                                                        size = 24
                                                                                )
                                                                        }
                                                                }

                                                                Column {
                                                                        Text(
                                                                                forecast.condition,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodyLarge,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Medium,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurface
                                                                        )

                                                                        // Quick weather stats
                                                                        Row(
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        16.dp
                                                                                                )
                                                                        ) {
                                                                                WeatherStat(
                                                                                        "💧",
                                                                                        forecast.chanceOfRain
                                                                                )
                                                                                WeatherStat(
                                                                                        "💨",
                                                                                        forecast.windSpeed
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }

                                                // Temperature section with better design
                                                Column(
                                                        horizontalAlignment = Alignment.End,
                                                        modifier = Modifier.weight(1f)
                                                ) {
                                                        Row(
                                                                verticalAlignment = Alignment.Top,
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(2.dp)
                                                        ) {
                                                                Text(
                                                                        forecast.highTemp.replace(
                                                                                "°C",
                                                                                ""
                                                                        ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .displaySmall,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface,
                                                                        fontSize = 32.sp
                                                                )
                                                                Text(
                                                                        "°",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .headlineSmall,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.6f
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        top = 4.dp
                                                                                )
                                                                )
                                                        }

                                                        Text(
                                                                forecast.lowTemp,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface.copy(
                                                                                alpha = 0.7f
                                                                        ),
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                // Enhanced running score
                                                EnhancedRunningScoreBadge(
                                                        score = forecast.runningScore,
                                                        onScoreClick = onScoreClick
                                                )
                                        }

                                        // Expandable details with enhanced design
                                        AnimatedVisibility(
                                                visible = isExpanded,
                                                enter =
                                                        slideInVertically(
                                                                initialOffsetY = { -it / 2 },
                                                                animationSpec =
                                                                        spring(
                                                                                dampingRatio =
                                                                                        Spring.DampingRatioMediumBouncy
                                                                        )
                                                        ) + fadeIn(),
                                                exit =
                                                        slideOutVertically(
                                                                targetOffsetY = { -it / 2 },
                                                                animationSpec =
                                                                        spring(
                                                                                dampingRatio =
                                                                                        Spring.DampingRatioMediumBouncy
                                                                        )
                                                        ) + fadeOut()
                                        ) {
                                                Column(modifier = Modifier.padding(top = 20.dp)) {
                                                        // Elegant divider
                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(1.dp)
                                                                                .background(
                                                                                        brush =
                                                                                                Brush.horizontalGradient(
                                                                                                        colors =
                                                                                                                listOf(
                                                                                                                        Color.Transparent,
                                                                                                                        MaterialTheme
                                                                                                                                .colorScheme
                                                                                                                                .onSurface
                                                                                                                                .copy(
                                                                                                                                        alpha =
                                                                                                                                                0.2f
                                                                                                                                ),
                                                                                                                        Color.Transparent
                                                                                                                )
                                                                                                )
                                                                                )
                                                        )

                                                        Spacer(modifier = Modifier.height(20.dp))

                                                        // Enhanced detail cards
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(12.dp)
                                                        ) {
                                                                EnhancedDetailCard(
                                                                        icon = "📅",
                                                                        label = "Date",
                                                                        value = forecast.date,
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                )
                                                                EnhancedDetailCard(
                                                                        icon = "🌡️",
                                                                        label = "Feels Like",
                                                                        value =
                                                                                "${forecast.highTemp.replace("°C", "")}°",
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                )
                                                                EnhancedDetailCard(
                                                                        icon = "🎯",
                                                                        label = "Score",
                                                                        value =
                                                                                "${forecast.runningScore}%",
                                                                        modifier =
                                                                                Modifier.weight(1f),
                                                                        valueColor =
                                                                                scoreGradientColors
                                                                                        .first()
                                                                )
                                                        }

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        // Running recommendation
                                                        RunningRecommendationCard(
                                                                forecast.runningScore
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun WeatherStat(icon: String, value: String) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
                Text(icon, fontSize = 12.sp)
                Text(
                        value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                )
        }
}

@Composable
fun EnhancedRunningScoreBadge(
        score: Int,
        modifier: Modifier = Modifier,
        onScoreClick: (() -> Unit)? = null
) {
        val hapticFeedback = LocalHapticFeedback.current
        val animatedScore by
                animateIntAsState(
                        targetValue = score,
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                        label = "score_animation"
                )

        val scale by
                animateFloatAsState(
                        targetValue = 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                ),
                        label = "scale_animation"
                )

        val backgroundColor =
                when {
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFF8BC34A)
                        score >= 40 -> Color(0xFFFF9800)
                        score >= 20 -> Color(0xFFFF5722)
                        else -> Color(0xFFf44336)
                }

        val backgroundColorAnimated by
                animateColorAsState(
                        targetValue = backgroundColor,
                        animationSpec = tween(durationMillis = 800),
                        label = "color_animation"
                )

        // Enhanced badge with glow effect
        Box(modifier = modifier.size(72.dp).scale(scale), contentAlignment = Alignment.Center) {
                // Glow effect background
                Box(
                        modifier =
                                Modifier.size(68.dp)
                                        .clip(CircleShape)
                                        .background(
                                                brush =
                                                        Brush.radialGradient(
                                                                colors =
                                                                        listOf(
                                                                                backgroundColorAnimated
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.3f
                                                                                        ),
                                                                                Color.Transparent
                                                                        ),
                                                                radius = 100f
                                                        )
                                        )
                )

                // Main badge
                Surface(
                        modifier =
                                Modifier.size(64.dp).clickable {
                                        onScoreClick?.invoke()
                                        hapticFeedback.performHapticFeedback(
                                                androidx.compose.ui.hapticfeedback
                                                        .HapticFeedbackType.LongPress
                                        )
                                },
                        shape = CircleShape,
                        color = backgroundColorAnimated,
                        shadowElevation = 8.dp
                ) {
                        Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                        ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                                text = "$animatedScore",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                        )
                                        Text(
                                                text = "%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                        )
                                }
                        }
                }
        }
}

@Composable
fun EnhancedDetailCard(
        icon: String,
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
        Surface(
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                ) {
                        Text(icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                value,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = valueColor
                        )
                }
        }
}

@Composable
fun RunningRecommendationCard(score: Int) {
        val (recommendation, emoji, color) =
                when {
                        score >= 80 ->
                                Triple(
                                        "Perfect running weather! Get out there! 🎯",
                                        "🌟",
                                        Color(0xFF4CAF50)
                                )
                        score >= 60 ->
                                Triple("Great conditions for a run!", "👍", Color(0xFF8BC34A))
                        score >= 40 ->
                                Triple("Good enough for a moderate run", "👌", Color(0xFFFF9800))
                        score >= 20 ->
                                Triple("Consider indoor alternatives", "🤔", Color(0xFFFF5722))
                        else -> Triple("Better to skip today", "❌", Color(0xFFf44336))
                }

        Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
        ) {
                Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        Text(emoji, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        "Running Recommendation",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = color,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        recommendation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.8f
                                                ),
                                        fontWeight = FontWeight.Medium
                                )
                        }
                }
        }
}
