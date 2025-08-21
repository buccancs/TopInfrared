# TC001 Standalone Module

A simplified, standalone version of the TopInfrared application that focuses exclusively on TC001 basic IR camera functionality.

## Overview

This standalone module is a complete, self-contained Android application that provides:

- **English Only**: Single-language support for simplified development
- **TC001 Device Only**: Exclusive support for the basic IR camera (TC001)  
- **Local Recording**: All recordings saved locally, no cloud features
- **Minimal Dependencies**: Stripped down to essential components only

## Architecture

The standalone module follows a clean, modular architecture:

```
standalone/
├── app/                           # Main application module
│   ├── src/main/java/             # Application source code
│   │   ├── TC001Application       # Application class
│   │   ├── MainActivity           # Main entry point
│   │   ├── ThermalActivity        # Thermal imaging interface
│   │   ├── usb/                   # USB device management
│   │   └── thermal/               # Thermal processing
│   └── src/main/res/              # UI resources and layouts
├── libir-standalone/              # TC001 IR camera library
│   └── src/main/java/             # Camera handler implementation
├── common/                        # Shared utilities and constants
│   └── src/main/java/             # Common utilities
├── build.gradle                   # Module build configuration
└── settings.gradle               # Gradle settings
```

## Features

### Core Functionality
- **Real-time thermal imaging** through USB connection
- **Temperature measurement** with point, line, and area analysis modes
- **Live thermal visualization** with thermal color mapping
- **Image capture** - Save thermal images locally
- **Video recording** - Record thermal sessions locally

### Hardware Integration
- **USB Communication**: Direct connection to TC001 via USB
- **Device Detection**: Automatic TC001 device recognition
- **Permission Management**: Streamlined USB permission handling
- **Hot-plug Support**: Connect/disconnect TC001 during runtime

### User Interface
- **Simple Navigation**: Two-screen app (Main → Thermal)
- **Clean Design**: Material Design focused on thermal functionality
- **Touch Controls**: Intuitive thermal measurement mode switching
- **Status Indicators**: Clear connection and recording status

## Implementation Details

### Key Components

#### 1. TC001CameraHandler (`libir-standalone`)
- Simplified camera interface for TC001 device
- USB device communication and control
- Thermal frame processing and bitmap generation
- Mock implementation for development/demonstration

#### 2. TC001ThermalManager (`app/thermal`)
- High-level thermal capture management
- Image/video recording coordination
- Temperature measurement mode control
- File system integration for local storage

#### 3. TC001UsbManager (`app/usb`)
- USB device detection and enumeration
- Permission request and management
- Device connection lifecycle management
- TC001-specific device identification

### Configuration Files

#### AndroidManifest.xml Features
- USB device permissions and filters
- Storage permissions for local recording
- TC001 device filter configuration
- Activity declarations and navigation

#### Build Configuration
- Simplified dependency management
- English-only resource configuration
- TC001-focused build variants
- Minimal external dependencies

## Development Setup

### Prerequisites
- Android Studio 4.2+
- Android SDK 24+ (API level 24)
- TC001 thermal camera device (for hardware testing)
- USB-C or Micro-USB cable

### Build Instructions

```bash
# Navigate to standalone module
cd standalone/

# Clean and build debug APK
./gradlew clean assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### Testing

#### Without Hardware (Mock Mode)
The standalone app includes a mock thermal implementation that generates simulated thermal images for testing UI and functionality without actual TC001 hardware.

#### With TC001 Hardware
1. Connect TC001 device via USB
2. Grant USB permissions when prompted
3. Navigate through connection flow
4. Test thermal capture, measurement modes, and recording

### TC001 Device Integration

#### USB Device Identifiers
```kotlin
// Update these with actual TC001 VID/PID values
const val TC001_VENDOR_ID = 0x1234  // Replace with actual VID
const val TC001_PRODUCT_ID = 0x5678 // Replace with actual PID
```

#### Thermal Parameters
```kotlin
const val THERMAL_WIDTH = 256        // TC001 thermal resolution width
const val THERMAL_HEIGHT = 192       // TC001 thermal resolution height
const val THERMAL_FPS = 10           // Target frame rate
```

## File Structure

### Storage Organization
```
/Android/data/com.topinfrared.tc001.standalone/files/Pictures/
└── TC001_Thermal/
    ├── Images/                    # Captured thermal images
    │   ├── TC001_thermal_20231201_143022.jpg
    │   └── TC001_thermal_20231201_143045.jpg
    └── Videos/                    # Recorded thermal videos  
        ├── TC001_recording_20231201_143100.mp4
        └── TC001_recording_20231201_143200.mp4
```

### File Naming Convention
- **Images**: `TC001_thermal_YYYYMMDD_HHMMSS.jpg`
- **Videos**: `TC001_recording_YYYYMMDD_HHMMSS.mp4`

## Customization

### Adding TC001 Hardware Support
1. Update `TC001Constants.kt` with actual device VID/PID
2. Implement real thermal processing in `TC001CameraHandler`
3. Add native processing libraries if required
4. Configure device-specific thermal parameters

### UI Customization
- **Colors**: Update `colors.xml` with TC001 branding
- **Layouts**: Modify layouts for specific screen requirements  
- **Strings**: All strings in `strings.xml` for localization
- **Themes**: Material Design theming in `themes.xml`

### Feature Extensions
- **Export formats**: Add additional image/video formats
- **Analysis tools**: Implement thermal analysis features
- **Calibration**: Add thermal calibration functionality
- **Reports**: Generate thermal analysis reports

## Deployment

### APK Signing
Configure release signing in `app/build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file('path/to/keystore.jks')
            storePassword 'store_password'
            keyAlias 'key_alias' 
            keyPassword 'key_password'
        }
    }
}
```

### Distribution
- **Direct Install**: APK file for direct device installation
- **Enterprise**: Internal app distribution systems
- **Testing**: Development builds for testing and validation

## Comparison with Full TopInfrared

| Feature | Full TopInfrared | TC001 Standalone |
|---------|------------------|------------------|
| **Devices** | Multiple thermal devices | TC001 only |
| **Languages** | Multi-language support | English only |
| **Storage** | Cloud + Local | Local only |
| **Dependencies** | Complex ecosystem | Minimal |
| **Size** | ~50MB+ | ~10-15MB |
| **Setup** | Full configuration | Plug-and-play |
| **Updates** | App store updates | Direct APK |

## Benefits

### For Development
- **Faster Iteration**: Simplified codebase for rapid development
- **Easier Testing**: Single device focus reduces complexity
- **Clear Architecture**: Simple, understandable structure
- **Reduced Dependencies**: Fewer external libraries and services

### For Users  
- **Simple Setup**: No complex configuration required
- **Reliable**: Fewer failure points and dependencies
- **Privacy**: All data stays local, no cloud connectivity
- **Performance**: Optimized for TC001-specific workflows

### For Deployment
- **Smaller APK**: Reduced app size for distribution
- **Offline**: No network requirements for core functionality
- **Enterprise**: Easy to customize for specific environments
- **Maintenance**: Simpler update and support processes

## Support

For technical support or questions about the TC001 standalone module:

1. **Hardware Issues**: Check USB connection and device permissions
2. **Build Issues**: Verify Android SDK and build tools installation  
3. **Integration**: Review TC001 device identification and communication
4. **Customization**: Refer to component documentation and code comments

The standalone module provides a focused, efficient solution for TC001 thermal imaging applications while maintaining the core functionality and user experience of the full TopInfrared platform.