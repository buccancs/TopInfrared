#!/bin/bash

# TC001 Standalone Setup Script
# This script helps set up the development environment and build the standalone module

set -e

echo "🔥 TC001 Standalone Setup Script"
echo "================================="

# Check if we're in the right directory
if [[ ! -f "settings.gradle" ]]; then
    echo "❌ Error: Please run this script from the standalone/ directory"
    exit 1
fi

echo "✅ Found standalone module directory"

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1-2)
echo "☕ Java version: $JAVA_VERSION"

if [[ "$JAVA_VERSION" < "1.8" ]]; then
    echo "❌ Error: Java 8 or higher is required"
    exit 1
fi

echo "✅ Java version is compatible"

# Make gradlew executable
chmod +x gradlew
echo "✅ Made gradlew executable"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean > /dev/null 2>&1
echo "✅ Clean completed"

# Build the project
echo "🔨 Building TC001 standalone module..."
./gradlew build

if [[ $? -eq 0 ]]; then
    echo "✅ Build successful!"
    
    # Check for APKs
    DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
    RELEASE_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
    
    echo ""
    echo "📱 Generated APKs:"
    if [[ -f "$DEBUG_APK" ]]; then
        APK_SIZE=$(du -h "$DEBUG_APK" | cut -f1)
        echo "   • Debug APK: $DEBUG_APK ($APK_SIZE)"
    fi
    
    if [[ -f "$RELEASE_APK" ]]; then
        APK_SIZE=$(du -h "$RELEASE_APK" | cut -f1)
        echo "   • Release APK: $RELEASE_APK ($APK_SIZE)"
    fi
    
    echo ""
    echo "🎉 Setup completed successfully!"
    echo ""
    echo "📋 Next steps:"
    echo "   1. Install the APK on your Android device"
    echo "   2. Connect your TC001 thermal camera via USB"
    echo "   3. Launch the 'TC001 Standalone' app"
    echo "   4. Grant USB permissions when prompted"
    echo "   5. Start thermal imaging!"
    echo ""
    echo "📚 For detailed instructions, see:"
    echo "   • README.md - Technical documentation"
    echo "   • QUICKSTART.md - User setup guide"
    
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi