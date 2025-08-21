# Changelog

All notable changes to TopInfrared (IRCamera) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Enhanced AI-powered thermal analysis
- Support for additional thermal camera models
- Cloud-based thermal data synchronization
- Advanced 3D thermal mapping features

## [1.10.000] - 2024-08-21

### Added
- **3D Thermal Visualization**: New 3D representation module for thermal data
- **Enhanced PDF Reports**: Improved report generation with watermarks and professional layouts
- **Multi-language Support**: Extended localization for Chinese market (热视界)
- **Advanced Temperature Analysis**: New polygon and area-based temperature measurement tools
- **Cloud Integration**: Firebase analytics and crash reporting for better user experience
- **Hardware Support**: Extended compatibility with newer thermal imaging devices
- **Bluetooth LE Improvements**: More stable and efficient device communication
- **File Management**: Comprehensive data organization and export capabilities

### Changed
- **UI Modernization**: Updated interface following Material Design 3 guidelines
- **Performance Optimization**: Reduced memory usage and improved processing speed
- **Build System**: Updated to Android Gradle Plugin 7.1.3 and Kotlin 1.7.20
- **Target SDK**: Upgraded to API 34 (Android 14) with backward compatibility to API 24
- **Architecture**: Improved modular architecture with better separation of concerns

### Fixed
- **Bluetooth Connection**: Resolved timeout issues with Samsung devices
- **Thermal Calibration**: Fixed temperature accuracy issues on specific hardware models
- **Memory Leaks**: Addressed memory management issues in long-running sessions
- **UI Responsiveness**: Improved app responsiveness during thermal data processing
- **Permission Handling**: Better Android 10+ permission management for Bluetooth and storage

### Security
- **Data Encryption**: Enhanced encryption for thermal measurement data
- **Privacy Compliance**: GDPR and regional privacy law compliance improvements
- **Secure Communication**: Improved BLE security protocols

## [1.9.5] - 2024-06-15

### Added
- Support for HIK thermal cameras
- Real-time temperature streaming
- Thermal image export functionality

### Fixed
- Crash on Android 14 devices
- Bluetooth pairing issues on Pixel devices
- Temperature unit conversion accuracy

## [1.9.0] - 2024-04-10

### Added
- **Major UI Overhaul**: Complete interface redesign with modern Material Design
- **Cloud Backup**: Automatic thermal data backup to cloud storage
- **Advanced Analytics**: Enhanced temperature analysis algorithms
- **Multi-device Support**: Simultaneous connection to multiple thermal devices
- **Custom Palettes**: User-defined thermal color palettes

### Changed
- Migrated to AndroidX libraries
- Improved Bluetooth stack reliability
- Enhanced thermal processing algorithms
- Updated dependency versions for security

### Removed
- Legacy thermal device support (pre-2018 models)
- Deprecated API endpoints

## [1.8.2] - 2024-02-28

### Fixed
- Critical thermal calibration bug affecting temperature readings
- App crash when switching between thermal devices rapidly
- Memory leak in thermal image processing pipeline

### Security
- Updated third-party libraries to address security vulnerabilities

## [1.8.0] - 2024-01-15

### Added
- **Advanced Thermal Analysis**: New measurement tools and algorithms
- **Batch Processing**: Process multiple thermal images simultaneously
- **Enhanced Reporting**: Detailed thermal analysis reports with charts
- **API Integration**: RESTful API for external tool integration

### Changed
- Improved thermal image processing performance by 40%
- Enhanced user interface for better accessibility
- Updated thermal device detection algorithms

## [1.7.3] - 2023-11-20

### Fixed
- Thermal device connection stability issues
- Incorrect temperature readings in extreme environments
- App freezing during large thermal image processing

## [1.7.0] - 2023-10-05

### Added
- **Multi-language Support**: Initial localization for Chinese, Spanish, and German
- **Thermal Video Recording**: Record thermal video sessions
- **Advanced Filters**: New thermal image enhancement filters
- **Data Export**: Export thermal data in CSV format

### Changed
- Redesigned settings interface for better usability
- Improved thermal color mapping algorithms
- Enhanced Bluetooth Low Energy communication

## [1.6.5] - 2023-08-18

### Fixed
- App crash on devices with limited RAM
- Bluetooth connection drops during extended use
- Thermal image distortion on certain device orientations

### Security
- Implemented secure storage for user thermal data
- Enhanced authentication mechanisms

## [1.6.0] - 2023-07-01

### Added
- **3D Thermal Visualization**: Initial 3D thermal mapping capabilities
- **Temperature Trending**: Historical temperature data analysis
- **Custom Measurement Areas**: User-defined thermal measurement zones
- **Improved File Management**: Better organization of thermal images and data

### Changed
- Upgraded to Android API 33 (Android 13)
- Improved thermal sensor calibration process
- Enhanced battery optimization for thermal devices

## [1.5.2] - 2023-05-10

### Fixed
- Thermal device compatibility issues with newer Android versions
- Incorrect temperature scaling in certain measurement modes
- App performance issues on older devices

## [1.5.0] - 2023-04-01

### Added
- **Enhanced PDF Reporting**: Professional thermal analysis reports
- **Temperature Alerts**: Configurable temperature threshold notifications
- **Thermal Image Gallery**: Organized storage and viewing of thermal images
- **Device Diagnostics**: Built-in thermal device health monitoring

### Changed
- Improved thermal image processing algorithms
- Updated user interface for better thermal data visualization
- Enhanced Bluetooth device discovery and pairing

### Removed
- Support for Android API levels below 24

## [1.4.8] - 2023-02-15

### Fixed
- Critical thermal calibration errors
- App stability issues during thermal device switching
- Memory management improvements

## [1.4.0] - 2023-01-01

### Added
- **Real-time Thermal Streaming**: Live thermal image feed
- **Temperature Measurement Tools**: Point, line, and area measurement
- **Thermal Image Enhancement**: Filters and processing tools
- **User Profiles**: Personalized settings and preferences

### Changed
- Migrated to Kotlin as primary development language
- Improved thermal device communication protocols
- Enhanced app performance and stability

## [1.3.0] - 2022-11-01

### Added
- **Multi-device Support**: Connect multiple thermal cameras simultaneously
- **Thermal Data Logging**: Continuous temperature data recording
- **Advanced Calibration**: Improved thermal sensor calibration tools
- **Cloud Synchronization**: Basic cloud storage for thermal data

### Fixed
- Thermal image corruption issues
- Bluetooth connectivity problems on specific devices
- App crashes during thermal video processing

## [1.2.0] - 2022-09-01

### Added
- **Bluetooth Low Energy Support**: Connect wireless thermal devices
- **Thermal Image Processing**: Basic image enhancement and filtering
- **Data Export**: Export thermal measurements and images
- **User Settings**: Customizable app preferences and thermal parameters

### Changed
- Updated Android target SDK to API 31
- Improved thermal data visualization
- Enhanced user interface responsiveness

## [1.1.0] - 2022-07-01

### Added
- **Thermal Camera Integration**: Support for USB thermal cameras
- **Temperature Measurement**: Basic point temperature measurement
- **Image Capture**: Capture and save thermal images
- **Basic Reporting**: Simple thermal measurement reports

### Fixed
- Initial stability and performance issues
- USB device detection problems
- Temperature accuracy calibration

## [1.0.0] - 2022-05-01

### Added
- **Initial Release**: Basic thermal imaging application
- **Device Connection**: USB thermal device support
- **Live Preview**: Real-time thermal camera feed
- **Basic Controls**: Temperature range and color palette controls
- **Android Support**: Compatible with Android 7.0+ (API 24+)

---

## Legend

- **Added** for new features
- **Changed** for changes in existing functionality
- **Deprecated** for soon-to-be removed features
- **Removed** for now removed features
- **Fixed** for any bug fixes
- **Security** for vulnerability fixes

For more detailed information about any release, please check the corresponding GitHub release notes or contact our support team.