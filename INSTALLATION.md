# TopInfrared Installation Guide 🛠️

This guide provides detailed instructions for setting up TopInfrared development environment and installing the application.

## 📋 System Requirements

### Development Environment

#### Minimum Requirements
- **Operating System**: Windows 10/11, macOS 10.14+, or Ubuntu 18.04+
- **RAM**: 8GB (16GB recommended for optimal performance)
- **Storage**: 10GB free space for Android Studio and SDK
- **Internet**: Stable connection for downloading dependencies

#### Required Software
- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **JDK**: OpenJDK 8 or 11 (Android Studio includes embedded JDK)
- **Git**: Latest stable version
- **Android SDK**: API Level 24-34
- **Android NDK**: Version 21.3.6528147

### Target Device Requirements

#### Minimum Device Specifications
- **Android Version**: API 24 (Android 7.0) or higher
- **RAM**: 4GB minimum (6GB+ recommended)
- **Storage**: 2GB free space
- **Architecture**: ARM64-v8a (primary), ARMv7 (limited support)
- **Bluetooth**: 4.0+ for device connectivity
- **USB**: USB Host support (for wired thermal cameras)

#### Recommended Device Specifications
- **Android Version**: API 30+ (Android 11+)
- **RAM**: 8GB+ for complex thermal analysis
- **Storage**: 8GB+ for extensive data storage
- **Display**: 1080p+ resolution for detailed thermal imaging
- **Processing**: Snapdragon 660+ or equivalent

## 🔧 Development Setup

### 1. Install Android Studio

#### Windows
1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Run the installer and follow the setup wizard
3. Install Android SDK components when prompted

#### macOS
1. Download the DMG file from Android Studio website
2. Drag Android Studio to Applications folder
3. Run Android Studio and complete the setup wizard

#### Linux (Ubuntu/Debian)
```bash
# Add repository
sudo apt update
sudo apt install software-properties-common apt-transport-https wget

# Download and install
wget https://dl.google.com/dl/android/studio/ide-zips/2022.1.1.21/android-studio-2022.1.1.21-linux.tar.gz
tar -xzf android-studio-*.tar.gz
sudo mv android-studio /opt/
sudo ln -sf /opt/android-studio/bin/studio.sh /usr/local/bin/android-studio

# Launch
android-studio
```

### 2. Configure Android SDK

#### SDK Components
Install the following components through SDK Manager:
- **Android SDK Build-Tools**: 30.0.3+
- **Android SDK Platform-Tools**: Latest
- **Android SDK Tools**: Latest
- **Android API 24-34**: All required API levels
- **Android NDK**: Version 21.3.6528147
- **Intel x86 Emulator Accelerator (HAXM)**: For emulator performance

#### Environment Variables
Add to your system PATH:
```bash
# Windows (System Environment Variables)
ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
ANDROID_NDK_HOME=%ANDROID_HOME%\ndk\21.3.6528147

# macOS/Linux (add to ~/.bashrc or ~/.zshrc)
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/21.3.6528147
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### 3. Clone and Setup Project

#### Clone Repository
```bash
git clone https://github.com/buccancs/TopInfrared.git
cd TopInfrared
```

#### Project Configuration
```bash
# Make gradlew executable (macOS/Linux)
chmod +x gradlew

# Verify Gradle setup
./gradlew --version

# Initial project sync
./gradlew clean
```

### 4. IDE Configuration

#### Android Studio Settings
1. **Open the project** in Android Studio
2. **Wait for Gradle sync** to complete
3. **Configure NDK path**: File → Project Structure → SDK Location → Android NDK location
4. **Enable developer options**: File → Settings → Appearance & Behavior → System Settings → Android SDK

#### Recommended Plugins
- **Kotlin Multiplatform Mobile**: For Kotlin support
- **Firebase**: For analytics and crash reporting
- **Bluetooth Developer Tools**: For BLE debugging
- **Memory Profiler**: For performance optimization

## 📱 Application Installation

### Development Installation

#### Install Debug Build
```bash
# Connect Android device with USB debugging enabled
adb devices

# Install development debug build
./gradlew installDevDebug

# Or specific flavor
./gradlew installBetaDebug
./gradlew installProdDebug
```

#### Install Release Build
```bash
# Build and install release version
./gradlew installDevRelease
./gradlew installProdRelease
```

### Production Installation

#### From APK File
1. **Enable Unknown Sources** in device settings
2. **Download APK** from releases or build locally:
   ```bash
   ./gradlew assembleProdRelease
   ```
3. **Install APK** via ADB or file manager:
   ```bash
   adb install app/build/outputs/apk/prod/release/TopInfrared*.apk
   ```

#### From Google Play Store
- Search for "IRCamera" or "TopInfrared"
- Install the official version
- Note: May have different features than development builds

## 🔌 Hardware Setup

### Bluetooth Thermal Devices

#### Pairing Process
1. **Enable Bluetooth** on Android device
2. **Put thermal device** in pairing mode
3. **Open TopInfrared app**
4. **Navigate to device settings**
5. **Select "Add Device"**
6. **Choose your thermal device** from the list
7. **Follow pairing instructions**

#### Supported Devices
- HIK thermal imaging cameras
- Generic Bluetooth thermal sensors
- TOPDON thermal measurement tools

### USB Thermal Devices

#### Connection Setup
1. **Enable USB debugging** on Android device
2. **Connect thermal device** via USB cable
3. **Grant USB permissions** when prompted
4. **Verify connection** in app settings

#### Requirements
- USB Host capable Android device
- Compatible USB thermal camera
- Appropriate USB adapter (if needed)

## 🌐 Network Configuration

### Internet Requirements
- **Initial setup**: Download app data and updates
- **Cloud features**: Analytics, crash reporting, cloud storage
- **Updates**: App and thermal device firmware updates

### Offline Capabilities
- Core thermal imaging functions work offline
- Local data storage and processing
- Bluetooth device communication
- Report generation and export

### Firewall/Network Settings
If using on corporate networks, ensure these domains are accessible:
- `firebase.google.com` (Analytics and crash reporting)
- `dl.google.com` (Google Play Services)
- `topdon.com` (App services and support)
- `maven.google.com` (Development dependencies)

## 🔧 Troubleshooting Installation

### Common Development Issues

#### Gradle Build Failures
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew clean

# Re-download dependencies
./gradlew build --refresh-dependencies
```

#### NDK Issues
1. Verify NDK version matches project configuration
2. Check ANDROID_NDK_HOME environment variable
3. Reinstall NDK from SDK Manager

#### Signing Key Issues
```bash
# Generate debug keystore (if missing)
keytool -genkey -v -keystore debug.keystore -alias androiddebugkey \
        -keyalg RSA -keysize 2048 -validity 10000
```

### Common Runtime Issues

#### Bluetooth Connection Problems
1. **Restart Bluetooth** on device
2. **Clear Bluetooth cache**: Settings → Apps → Bluetooth → Storage → Clear Cache
3. **Remove and re-pair** thermal device
4. **Check device compatibility** list

#### Permission Issues
1. **Grant all required permissions** in app settings
2. **Enable location services** (required for Bluetooth on Android 10+)
3. **Check USB debugging** is enabled for USB devices

#### Performance Issues
1. **Free up storage space** (minimum 1GB recommended)
2. **Close background apps**
3. **Restart device** if performance is sluggish
4. **Check thermal device battery** level

### Device-Specific Issues

#### Samsung Devices
- Enable "Developer Options"
- Disable "MIUI Optimization" if present
- Check "Background App Limits"

#### Huawei/Honor Devices
- Disable "PowerGenie" for TopInfrared
- Add app to "Startup Manager" whitelist
- Enable "Stay connected when device sleeps"

#### Xiaomi Devices
- Turn off "MIUI Optimization"
- Add app to "Autostart" permissions
- Disable "Battery optimization" for the app

## 📞 Getting Help

### Documentation Resources
- **README.md**: Complete project overview
- **CONTRIBUTING.md**: Development guidelines
- **GitHub Wiki**: Detailed technical documentation
- **API Documentation**: Generated from source code

### Support Channels
- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and help
- **Email Support**: support@topdon.com
- **Technical Support**: Phone 1-833-629-4832

### Community Resources
- **Developer Forums**: Android thermal imaging community
- **Stack Overflow**: Tag questions with `topinfrared` or `thermal-imaging`
- **Reddit**: r/AndroidDev for Android development questions

---

**Need additional help?** Contact our support team or check the troubleshooting section in the main README.