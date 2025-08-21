# Bucika GSR - Standalone TopInfrared Version

## Overview

This is a standalone version of TopInfrared specifically designed for the bucika_gsr project. It combines thermal infrared imaging capabilities with Galvanic Skin Response (GSR) monitoring for multi-modal physiological analysis.

## Key Features

### Hardware Support
- **TC001 Device Only**: Simplified device support focusing only on TC001 thermal imaging
- **Shimmer GSR Sensors**: Integrated support for Shimmer wearable GSR sensors via ShimmerAndroidAPI
- **Bluetooth Connectivity**: Wireless connection to Shimmer devices

### Software Features
- **English Only Interface**: Simplified language support for English only
- **Real-time GSR Monitoring**: 128 Hz sampling rate for high-precision GSR data collection
- **Thermal IR Integration**: Combined thermal imaging with physiological monitoring
- **Multi-modal Data Sync**: Synchronized collection of thermal and GSR data

### Excluded Components
- House module (removed)
- Edit3D module (removed)
- Multi-language support (English only)
- Multi-device support (TC001 only)

## Architecture

The bucika_gsr version includes these essential modules:

- `app/` - Main application
- `libapp/` - Core application library
- `libcom/` - Common utilities
- `libir/` - Infrared imaging library
- `libui/` - UI components
- `libmenu/` - Menu system
- `component/thermal-ir/` - Thermal infrared component
- `BleModule/` - Bluetooth Low Energy module

## GSR Integration

### GSRManager
- Manages Shimmer device connections
- Handles GSR data streaming at 128 Hz
- Provides real-time data callbacks
- Supports device auto-discovery

### GSRActivity
- User interface for GSR monitoring
- Connection management controls
- Real-time data visualization
- Recording controls

## Getting Started

1. Build the project using Android Studio
2. Connect a TC001 thermal imaging device
3. Pair with a Shimmer GSR sensor via Bluetooth
4. Launch the app and navigate to "GSR Monitoring"
5. Start synchronized thermal and GSR recording

## Requirements

- Android SDK 22+
- TC001 thermal imaging device
- Shimmer GSR sensor with Bluetooth capability
- Bluetooth and location permissions

## Dependencies

- ShimmerAndroidAPI for GSR sensor integration
- Standard TopInfrared libraries for thermal imaging
- Android Bluetooth APIs for wireless connectivity

## Data Output

The system provides synchronized data streams:
- Thermal IR images from TC001
- GSR conductance values (µS) at 128 Hz
- Skin temperature measurements
- Timestamp synchronization for multi-modal analysis

## Use Cases

This version is designed for research applications requiring:
- Physiological stress monitoring
- Emotional response analysis
- Multi-modal sensor fusion experiments
- Contactless vs contact-based measurement comparison

## Support

This is a standalone version specifically created for the bucika_gsr project requirements.