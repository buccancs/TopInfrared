@echo off

REM Standalone Build Script for Windows
REM This script helps compile the standalone project

echo === TC001 Standalone Compilation Script ===
echo Project: TC001-Standalone - TopInfrared Module
echo ============================================

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ Error: gradlew.bat not found. Make sure you're in the standalone directory.
    pause
    exit /b 1
)

echo 🧹 Cleaning project...
call gradlew.bat clean

echo.
echo 🔨 Available build tasks:
echo   1. assembleDebug    - Build debug APK
echo   2. assembleRelease  - Build release APK
echo   3. build           - Build all variants
echo   4. tasks           - Show all available tasks
echo.

REM If parameter provided, run specific task
if "%1"=="" (
    echo Usage: %0 [task]
    echo Example: %0 assembleDebug
    echo.
    echo 🏗️  Building debug APK by default...
    call gradlew.bat assembleDebug
) else (
    echo 🏗️  Running: gradlew.bat %1
    call gradlew.bat %1
)

if %errorlevel% equ 0 (
    echo.
    echo ✅ Build completed successfully!
    echo 📱 APK location: app\build\outputs\apk\
    echo.
    echo 🔧 Features included in standalone:
    echo    • TC001 USB thermal imaging device
    echo    • Material Design 3 UI
    echo    • Local file recording and management
    echo    • Samsung ISP Stage 3 processing
    echo    • English-only interface
    echo    • Modern Android SDK 34 support
) else (
    echo.
    echo ❌ Build failed. Check the output above for details.
    echo.
    echo 🛠️  Common issues:
    echo    • Missing dependencies: Run 'gradlew --refresh-dependencies'
    echo    • Android SDK not found: Check ANDROID_HOME environment variable
    echo    • Build tools missing: Install Android SDK Build Tools
)

pause