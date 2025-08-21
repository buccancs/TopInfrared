# Bucika GSR - Run Configurations

This directory contains IDE run configurations and build scripts for the **Bucika GSR** project.

## Overview

The Bucika GSR is a standalone version of TopInfrared specifically created for GSR (Galvanic Skin Response) sensor integration. It combines thermal imaging with physiological monitoring.

## Quick Start

### Option 1: Using Build Scripts

**Linux/macOS:**
```bash
# Build debug APK
./build.sh assembleDebug

# Build release APK  
./build.sh assembleRelease

# Clean project
./build.sh clean
```

**Windows:**
```cmd
# Build debug APK
build.bat assembleDebug

# Build release APK
build.bat assembleRelease

# Clean project
build.bat clean
```

### Option 2: Using IntelliJ IDEA/Android Studio

1. Open the `bucika_gsr` folder in Android Studio/IntelliJ IDEA
2. Wait for Gradle sync to complete
3. Use the pre-configured run configurations:

#### Available Run Configurations

- **bucika_gsr_app**: Run the Android application on device/emulator
- **bucika_gsr:assembleDebug**: Build debug APK
- **bucika_gsr:assembleRelease**: Build release APK  
- **bucika_gsr:clean**: Clean build artifacts

### Option 3: Manual Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Show all available tasks
./gradlew tasks
```

## Project Features

- **TC001 thermal imaging device support**
- **Shimmer GSR sensor integration** 
- **Bluetooth Low Energy connectivity**
- **Real-time GSR data at 128 Hz**
- **Synchronized thermal + GSR recording**
- **Android SDK 22+ compatibility**

## Architecture

```
bucika_gsr/
├── app/                    - Main application
├── libapp/                 - Core application library  
├── libcom/                 - Common utilities
├── libir/                  - Infrared imaging library
├── libui/                  - UI components
├── libmenu/                - Menu system
├── component/thermal-ir/   - Thermal infrared component
├── BleModule/              - Bluetooth Low Energy module
├── build.sh               - Linux/macOS build script
├── build.bat              - Windows build script
└── .idea/runConfigurations/ - IDE run configurations
```

## Requirements

- Android SDK 22+
- TC001 thermal imaging device
- Shimmer GSR sensor with Bluetooth capability
- JDK 8+ for compilation
- Android Studio/IntelliJ IDEA (recommended)

## Troubleshooting

### Build Issues

If you encounter compilation errors:

1. **Missing dependencies**: Run `./gradlew --refresh-dependencies`
2. **Android SDK not found**: Set `ANDROID_HOME` environment variable
3. **Missing string resources**: Some localization strings may need to be added

### Common Solutions

```bash
# Refresh dependencies
./gradlew --refresh-dependencies

# Clean and rebuild
./gradlew clean assembleDebug

# Check project structure
./gradlew projects
```

## Data Output

The system provides synchronized data streams:
- **Thermal IR images** from TC001
- **GSR conductance values** (µS) at 128 Hz  
- **Skin temperature measurements**
- **Timestamp synchronization** for multi-modal analysis

## Use Cases

This version is designed for research applications requiring:
- Physiological stress monitoring
- Emotional response analysis  
- Multi-modal sensor fusion experiments
- Contactless vs contact-based measurement comparison