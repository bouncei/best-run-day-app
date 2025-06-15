#!/bin/bash

echo "🏃‍♂️ Best Run Day App - Quick Start"
echo "=================================="
echo ""

# Check if Android SDK is installed
if ! command -v adb &> /dev/null; then
    echo "❌ Android SDK not found. Please install Android Studio first."
    echo "   Download from: https://developer.android.com/studio"
    exit 1
fi

# Check for connected devices
devices=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
if [ $devices -eq 0 ]; then
    echo "📱 No Android device or emulator detected."
    echo "   Please connect a device or start an emulator first."
    echo ""
    echo "To start an emulator:"
    echo "   1. Open Android Studio"
    echo "   2. Go to Tools > AVD Manager"
    echo "   3. Create or start a virtual device"
    exit 1
fi

echo "✅ Android device/emulator detected"
echo "🔧 Building and installing the app..."
echo ""

# Clean and build the project
./gradlew clean
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📲 Installing app on device..."
    
    # Install the APK
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    
    if [ $? -eq 0 ]; then
        echo "✅ App installed successfully!"
        echo "🚀 Launching Best Run Day App..."
        
        # Launch the app
        adb shell am start -n tech.bouncei.bestrunday/.MainActivity
        
        echo ""
        echo "🎉 App launched! You should see it on your device now."
        echo ""
        echo "📝 Note: The app is running in demo mode with sample data."
        echo "   To use real weather data:"
        echo "   1. Get a free API key from https://www.weatherapi.com/"
        echo "   2. Replace 'your_api_key_here' in WeatherApiService.kt"
        echo "   3. Rebuild and run the app"
    else
        echo "❌ Failed to install app"
    fi
else
    echo "❌ Build failed. Please check the error messages above."
fi 