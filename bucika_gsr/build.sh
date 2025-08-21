#!/bin/bash

# Bucika GSR Build Script
# This script helps compile the bucika_gsr project

echo "=== Bucika GSR Compilation Script ==="
echo "Project: BucikaGSR - Standalone TopInfrared Version with GSR"
echo "================================================="

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: gradlew not found. Make sure you're in the bucika_gsr directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "🧹 Cleaning project..."
./gradlew clean

echo ""
echo "🔨 Available build tasks:"
echo "  1. assembleDebug    - Build debug APK"
echo "  2. assembleRelease  - Build release APK (requires signing)"
echo "  3. build           - Build all variants"
echo "  4. tasks           - Show all available tasks"
echo ""

# If parameter provided, run specific task
if [ $# -eq 0 ]; then
    echo "Usage: $0 [task]"
    echo "Example: $0 assembleDebug"
    echo ""
    echo "🏗️  Building debug APK by default..."
    ./gradlew assembleDebug
else
    echo "🏗️  Running: ./gradlew $1"
    ./gradlew $1
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build completed successfully!"
    echo "📱 APK location: app/build/outputs/apk/"
    echo ""
    echo "🔧 Features included in bucika_gsr:"
    echo "   • TC001 thermal imaging device support"
    echo "   • Shimmer GSR sensor integration"  
    echo "   • Bluetooth Low Energy connectivity"
    echo "   • Real-time GSR data at 128 Hz"
    echo "   • Synchronized thermal + GSR recording"
else
    echo ""
    echo "❌ Build failed. Check the output above for details."
    echo ""
    echo "🛠️  Common issues:"
    echo "   • Missing dependencies: Run 'gradlew --refresh-dependencies'"
    echo "   • Android SDK not found: Check ANDROID_HOME environment variable"
    echo "   • Missing string resources: Some strings may need to be added"
    exit 1
fi