# TC001 Standalone - Quick Start Guide

## Overview
This guide walks you through setting up and using the TC001 Standalone thermal imaging app.

## System Requirements

### Hardware
- Android device with USB Host support (Android 6.0+)
- TC001 thermal imaging camera
- USB OTG adapter (if needed for your device)
- Micro-USB or USB-C cable (depending on TC001 model)

### Software  
- Android 6.0 (API 24) or higher
- ~50MB free storage space
- USB debugging enabled (for development builds)

## Installation

### Method 1: APK Installation (Recommended)
1. Download the latest `tc001-standalone-release.apk`
2. Enable "Unknown Sources" in Android Settings > Security
3. Open the APK file and tap "Install"
4. Grant required permissions when prompted

### Method 2: Development Build
```bash
# Clone the repository
git clone [repository-url]
cd TopInfrared/standalone

# Build and install
./gradlew installDebug
```

## First-Time Setup

### 1. Launch the App
- Open "TC001 Standalone" from your app drawer
- You'll see the main screen with connection status

### 2. Connect TC001 Device
- Connect TC001 camera to your Android device via USB
- If using USB OTG, connect the adapter first, then TC001
- The app will automatically detect the TC001 device

### 3. Grant Permissions
When prompted, grant the following permissions:
- **USB Permission**: Required for TC001 communication
- **Storage Permission**: Required for saving images/videos
- **Camera Permission**: Required for thermal capture

## Using TC001 Standalone

### Main Screen
- **Connection Status**: Shows TC001 connection state
- **Connect TC001**: Button to establish device connection  
- **Local Files**: Access captured images and recordings
- **Features List**: Overview of available functionality

### Thermal View (when TC001 is connected)
- **Live Thermal Feed**: Real-time thermal imaging display
- **Temperature Modes**: 
  - **Point**: Measure temperature at specific points
  - **Line**: Measure temperature along a line
  - **Area**: Measure temperature in selected areas
- **Capture**: Take thermal image snapshots
- **Record**: Start/stop thermal video recording

### Controls
- **Back Button**: Return to main screen
- **Mode Buttons**: Switch between Point/Line/Area measurement
- **Capture Button**: Save current thermal image
- **Record Button**: Toggle thermal video recording

## File Management

### Storage Location
All files are saved to:
```
/Android/data/com.topinfrared.tc001.standalone/files/Pictures/TC001_Thermal/
```

### File Types
- **Images**: `.jpg` format with timestamp naming
- **Videos**: `.mp4` format with timestamp naming
- **Naming**: `TC001_thermal_YYYYMMDD_HHMMSS.ext`

### Accessing Files
1. Tap "Local Files" on main screen
2. Use a file manager to navigate to the storage location
3. Share files via standard Android sharing options

## Troubleshooting

### TC001 Not Detected
- **Check Connection**: Ensure USB cable is properly connected
- **USB OTG**: Verify your device supports USB Host mode
- **Permissions**: Grant USB permission when prompted
- **Device Support**: Confirm TC001 model compatibility

### Permission Issues
- **USB Permission**: Always allow when prompted
- **Storage Permission**: Required for file saving
- **Retry**: Disconnect and reconnect TC001 if permission denied

### App Crashes
- **Restart App**: Close and reopen the application
- **Reboot Device**: Restart your Android device
- **Clear Data**: Clear app data in Android Settings > Apps
- **Reinstall**: Uninstall and reinstall the app

### Poor Thermal Quality
- **Clean Lens**: Ensure TC001 lens is clean
- **Warm-up**: Allow TC001 to warm up for optimal performance
- **Distance**: Maintain appropriate distance from subject
- **Environment**: Avoid direct sunlight or extreme temperatures

## Advanced Features

### Temperature Calibration
- The app uses default TC001 temperature calibration
- For precise measurements, refer to TC001 hardware documentation
- Professional calibration may require additional tools

### Recording Settings
- Videos are recorded at 10 FPS for optimal file size
- Maximum recording time depends on available storage
- Recordings are automatically stopped when storage is low

### File Export
- Use Android's built-in sharing to export files
- Compatible with email, cloud storage, and messaging apps
- Images can be opened in any photo viewing app
- Videos can be played in any video player app

## Performance Tips

### Battery Optimization
- TC001 device draws power from Android device
- Use external power bank for extended sessions
- Close other apps to conserve battery
- Lower screen brightness when not needed

### Storage Management  
- Regularly backup and delete old files
- Monitor storage usage in app settings
- Consider using external storage (SD card) if supported

### Connection Reliability
- Use high-quality USB cables
- Avoid cable extensions when possible
- Keep USB connectors clean and dry
- Replace cables if connection issues persist

## Support

### Common Issues
- **Device Not Found**: Check USB connection and permissions
- **App Won't Start**: Verify Android version compatibility (6.0+)
- **Files Not Saving**: Ensure storage permissions are granted
- **Poor Image Quality**: Clean TC001 lens and check environmental conditions

### Getting Help
1. **Check Documentation**: Review this guide and README.md
2. **Restart Process**: Try restarting app and reconnecting device
3. **Device Logs**: Enable developer options for detailed logging
4. **Report Issues**: Include device model, Android version, and error details

### Version Information
- **App Version**: Check in Android Settings > Apps > TC001 Standalone
- **TC001 Firmware**: Refer to TC001 hardware documentation
- **Android Version**: Settings > About Phone > Android Version

## Updates

### App Updates
- **Manual**: Download and install new APK files
- **Development**: Pull latest code and rebuild
- **Notifications**: Check for announcements and release notes

### TC001 Firmware
- Firmware updates for TC001 device are separate from app updates
- Follow TC001 manufacturer guidelines for firmware updates
- App compatibility is maintained across TC001 firmware versions

## Privacy & Data

### Data Collection
- **No Cloud Data**: All data stays on your device
- **No Analytics**: No usage tracking or analytics
- **No Network**: App works completely offline
- **Local Only**: Images and videos stored locally

### Data Security
- **Device Storage**: Files stored in private app directory
- **No Transmission**: No automatic data transmission
- **User Control**: Full control over file sharing and deletion
- **Permissions**: Only required permissions requested

This guide covers the essential setup and usage of TC001 Standalone. For technical details and customization, refer to the complete README.md documentation.