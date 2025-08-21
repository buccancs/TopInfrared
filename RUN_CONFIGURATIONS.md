# TopInfrared - Run Configurations Guide

This document explains how to compile and run the **bucika_gsr** and **standalone** modules of the TopInfrared project.

## Project Overview

This repository contains two main compilable modules:

### 1. Bucika GSR (`bucika_gsr/`)
- **Standalone TopInfrared Version with GSR integration**
- Combines thermal imaging with Galvanic Skin Response monitoring
- Supports Shimmer GSR sensors via Bluetooth
- Synchronized multi-modal data recording

### 2. TC001 Standalone (`standalone/`)
- **Complete TC001 thermal imaging application**
- Optimized for TC001 USB thermal devices
- Modern Material Design 3 interface
- Production-ready with Samsung ISP Stage 3 processing

## Quick Start

### Prerequisites

- **Android Studio** or **IntelliJ IDEA** with Android plugin
- **JDK 8+** for compilation
- **Android SDK 24+** (API Level 24 minimum)
- **ANDROID_HOME** environment variable set correctly

### Using Build Scripts (Recommended)

#### Standalone Module ✅ (Working)

```bash
cd standalone/
# Linux/macOS
./build.sh assembleDebug

# Windows  
build.bat assembleDebug
```

#### Bucika GSR Module ⚠️ (Has compilation issues)

```bash
cd bucika_gsr/
# Linux/macOS
./build.sh assembleDebug

# Windows
build.bat assembleDebug
```

### Using IDE Run Configurations

Both projects include pre-configured IntelliJ IDEA/Android Studio run configurations:

1. **Open project** in Android Studio/IntelliJ IDEA
2. **Wait for Gradle sync** to complete
3. **Select run configuration** from dropdown:
   - `bucika_gsr_app` / `standalone_app` - Run on device
   - `*:assembleDebug` - Build debug APK
   - `*:assembleRelease` - Build release APK
   - `*:clean` - Clean build artifacts

### Manual Gradle Commands

```bash
# In either bucika_gsr/ or standalone/ directory:

# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Clean build
./gradlew clean

# Show available tasks
./gradlew tasks
```

## Build Status

| Project | Build Status | Notes |
|---------|-------------|-------|
| **standalone** | ✅ **Working** | Successfully builds debug and release APKs |
| **bucika_gsr** | ⚠️ **Issues** | Missing string resources causing compilation errors |

## Project Features Comparison

| Feature | Standalone | Bucika GSR |
|---------|-----------|------------|
| **TC001 Device Support** | ✅ | ✅ |
| **Material Design 3** | ✅ | ❌ |
| **GSR Sensor Integration** | ❌ | ✅ |
| **Bluetooth Connectivity** | ❌ | ✅ |
| **Multi-modal Recording** | ❌ | ✅ |
| **Samsung ISP Stage 3** | ✅ | ✅ |
| **Production Ready** | ✅ | ⚠️ |

## Directory Structure

```
TopInfrared/
├── bucika_gsr/                    # GSR-enabled standalone version
│   ├── .idea/runConfigurations/   # IDE run configurations
│   ├── build.sh / build.bat      # Build scripts
│   ├── RUN_CONFIGURATIONS.md     # Detailed setup guide
│   └── ...                       # Project source files
│
├── standalone/                    # TC001 standalone application
│   ├── .idea/runConfigurations/   # IDE run configurations  
│   ├── build.sh / build.bat      # Build scripts
│   ├── RUN_CONFIGURATIONS.md     # Detailed setup guide
│   └── ...                       # Project source files
│
└── RUN_CONFIGURATIONS.md         # This file
```

## Troubleshooting

### Common Build Issues

1. **Android SDK not found**
   ```bash
   export ANDROID_HOME=/path/to/android/sdk
   ```

2. **Missing dependencies**
   ```bash
   ./gradlew --refresh-dependencies
   ```

3. **Build tools missing**
   - Install Android SDK Build Tools via Android Studio SDK Manager

### Project-Specific Issues

#### Bucika GSR
- ⚠️ **Missing string resources** - Some localization strings need to be added
- ⚠️ **Deprecated Kotlin extensions** - Uses deprecated kotlin-android-extensions plugin

#### Standalone  
- ✅ **All issues resolved** - Production ready

## Getting Help

For detailed setup instructions for each project:
- **Bucika GSR**: See `bucika_gsr/RUN_CONFIGURATIONS.md`
- **Standalone**: See `standalone/RUN_CONFIGURATIONS.md`

## Next Steps

1. **For Production Use**: Use the `standalone` module (fully working)
2. **For GSR Research**: Fix remaining issues in `bucika_gsr` module
3. **Development**: Use the provided IDE run configurations for easy debugging

## Build Output Locations

After successful compilation:
- **APK files**: `{project}/app/build/outputs/apk/`
- **AAR libraries**: `{project}/{module}/build/outputs/aar/`
- **Build reports**: `{project}/build/reports/`