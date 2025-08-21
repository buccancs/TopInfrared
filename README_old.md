# TopInfrared TC001 - Basic IR Camera Integration Guide 🌡️📱

> Focused documentation for TC001 basic thermal imaging camera integration and UI navigation

**TC001** is the basic thermal imaging camera device supported by the TopInfrared application. This guide covers the TC001-specific integration, UI navigation, and how the system works together to provide thermal imaging capabilities.

## 📱 TC001 Overview

**TC001** is a **line-connected** (wired/USB) basic infrared thermal imaging camera that provides:

- **Real-time thermal imaging** through USB connection
- **Temperature measurement** with point, line, and area analysis
- **Live thermal visualization** with customizable color palettes
- **Image capture and analysis** capabilities
- **Professional thermal reporting** features

### Device Specifications
- **Connection Type**: USB/Wired (Line connection)
- **Communication**: Direct USB communication via UVC (USB Video Class)
- **Resolution**: Standard thermal resolution for basic analysis
- **Power**: USB-powered (no external power required)

## 🔗 TC001 Integration Architecture

### Hardware Integration
```mermaid
graph TD
    A[TC001 Device] -->|USB Connection| B[Android Device]
    B --> C[libir Module]
    C --> D[IRUVCTC Camera Handler]
    D --> E[Thermal Image Processing]
    E --> F[IR Thermal Activity]
    F --> G[User Interface]
```

### Core Integration Components

#### 1. **libir Module** (`/libir/`)
- **Primary TC001 integration layer**
- Contains `IRUVCTC.java` - main camera handler for TC001
- Handles USB communication and device control
- Manages thermal data processing and image generation

#### 2. **Device Detection & Connection**
```kotlin
// Device type definition
enum class IRDeviceType {
    TC001 {
        override fun isLine(): Boolean = true  // USB/Line device
        override fun getDeviceName(): String = "TC001"
    }
}

// Connection checking
DeviceTools.isConnect() // Checks if TC001 is connected
```

#### 3. **Main Activity Classes**
- **`IRThermalNightActivity`**: Primary thermal imaging interface for TC001
- **`IRUVCTC.java`**: Low-level camera control and communication
- **Native processing**: NDK integration for real-time thermal processing

## 🏗️ Project Architecture

TopInfrared follows a modular Android architecture with clear separation of concerns:

### System Architecture Overview

```mermaid
graph TB
    subgraph "User Interface Layer"
        UI[UI Components]
        VM[ViewModels]
        AF[Activities & Fragments]
    end
    
    subgraph "Feature Modules"
        TH[thermal-hik]
        TIR[thermal-ir]
        TL[thermal-lite]
        T04[thermal04]
        T07[thermal07]
        E3D[edit3d]
        HOUSE[house]
        PSEUDO[pseudo]
        TRANSFER[transfer]
        USER[user]
    end
    
    subgraph "Library Modules"
        LIBAPP[libapp]
        LIBCOM[libcom]
        LIBHIK[libhik]
        LIBIR[libir]
        LIBMENU[libmenu]
        LIBUI[libui]
    end
    
    subgraph "Core Libraries"
        COMMON[commonlibrary]
        BLE[BleModule]
        COMP[CommonComponent]
    end
    
    subgraph "External Dependencies"
        HIK[HIK SDK]
        IR[IR Sensors]
        FB[Firebase]
        BT[Bluetooth LE]
    end
    
    UI --> VM
    VM --> AF
    AF --> TH
    AF --> TIR
    AF --> TL
    AF --> T04
    AF --> T07
    AF --> E3D
    
    TH --> LIBHIK
    TIR --> LIBIR
    TL --> LIBIR
    T04 --> LIBIR
    T07 --> LIBIR
    
    LIBHIK --> HIK
    LIBIR --> IR
    BLE --> BT
    
    TH --> COMMON
    TIR --> COMMON
    TL --> COMMON
    HOUSE --> COMMON
    PSEUDO --> COMMON
    TRANSFER --> COMMON
    USER --> COMMON
    
    COMMON --> LIBAPP
    COMMON --> LIBCOM
    COMMON --> LIBUI
    COMMON --> FB
    
    style UI fill:#e1f5fe
    style TH fill:#f3e5f5
    style TIR fill:#f3e5f5
    style COMMON fill:#e8f5e8
    style HIK fill:#fff3e0
    style IR fill:#fff3e0
```

### Module Dependency Graph

```mermaid
graph LR
    subgraph "Application Core"
        APP[app]
    end
    
    subgraph "Feature Components"
        TH[thermal-hik]
        TIR[thermal-ir]
        TL[thermal-lite]
        T04[thermal04]
        T07[thermal07]
        E3D[edit3d]
        HOUSE[house]
        PSEUDO[pseudo]
        TRANSFER[transfer]
        USER[user]
        CC[CommonComponent]
    end
    
    subgraph "Library Modules"
        CL[commonlibrary]
        LIBAPP[libapp]
        LIBCOM[libcom]
        LIBHIK[libhik]
        LIBIR[libir]
        LIBMENU[libmenu]
        LIBUI[libui]
        BLE[BleModule]
    end
    
    subgraph "Local Repository"
        LAC[libac020]
        LC[libcommon]
        LIR[libirutils]
    end
    
    APP --> TH
    APP --> TIR
    APP --> TL
    APP --> T04
    APP --> T07
    APP --> E3D
    APP --> HOUSE
    APP --> PSEUDO
    APP --> TRANSFER
    APP --> USER
    APP --> CC
    
    TH --> CL
    TIR --> CL
    TL --> CL
    T04 --> CL
    T07 --> CL
    E3D --> CL
    HOUSE --> CL
    PSEUDO --> CL
    TRANSFER --> CL
    USER --> CL
    CC --> CL
    
    TH --> LIBHIK
    TIR --> LIBIR
    TL --> LIBIR
    T04 --> LIBIR
    T07 --> LIBIR
    
    CL --> LIBAPP
    CL --> LIBCOM
    CL --> LIBUI
    CL --> LIBMENU
    CL --> BLE
    
    LIBIR --> LAC
    LIBIR --> LC
    LIBIR --> LIR
    
    style APP fill:#ff9800
    style CL fill:#4caf50
    style TH fill:#2196f3
    style TIR fill:#2196f3
```

### Directory Structure
```
TopInfrared/
├── app/                          # Main application module
├── component/                    # Feature modules
│   ├── edit3d/                  # 3D thermal visualization
│   ├── pseudo/                  # Pseudo-color processing
│   ├── thermal-hik/             # HIK device integration
│   ├── thermal-ir/              # IR sensor management
│   ├── thermal-lite/            # Lightweight thermal processing
│   ├── thermal04/               # Thermal sensor model 04
│   ├── thermal07/               # Thermal sensor model 07
│   ├── transfer/                # Data transfer utilities
│   └── user/                    # User management
├── BleModule/                   # Bluetooth Low Energy module
├── libcom/                      # Common utilities and PDF generation
├── libir/                       # Core infrared processing library
├── libui/                       # UI components and charting
├── libhik/                      # HIK device specific libraries
├── libmatrix/                   # Matrix operations for image processing
├── libmenu/                     # Menu and navigation components
└── LocalRepo/                   # Local dependencies and utilities
```

### Application Flow Diagram

```mermaid
flowchart TD
    START([App Launch]) --> INIT[Initialize Components]
    INIT --> PERM{Check Permissions}
    PERM -->|Missing| REQ[Request Permissions]
    REQ --> PERM
    PERM -->|Granted| MAIN[Main Dashboard]
    
    MAIN --> SCAN[Scan for Devices]
    MAIN --> VIEW[View Saved Images]
    MAIN --> SETTINGS[Settings]
    
    SCAN --> DEV{Device Found?}
    DEV -->|Yes| CONNECT[Connect to Device]
    DEV -->|No| SCAN
    
    CONNECT --> SUCCESS{Connection Success?}
    SUCCESS -->|Yes| LIVE[Live Thermal Feed]
    SUCCESS -->|No| ERROR[Show Error]
    ERROR --> SCAN
    
    LIVE --> CAPTURE[Capture Image]
    LIVE --> MEASURE[Temperature Measurement]
    LIVE --> ANALYZE[Analysis Tools]
    
    CAPTURE --> SAVE[Save to Gallery]
    MEASURE --> REPORT[Generate Report]
    ANALYZE --> VISUALIZE[3D Visualization]
    
    SAVE --> EXPORT[Export Options]
    REPORT --> PDF[Generate PDF]
    VISUALIZE --> EDIT[Edit & Annotate]
    
    EXPORT --> SHARE[Share/Cloud Upload]
    PDF --> SHARE
    EDIT --> SAVE
    
    style START fill:#4caf50
    style MAIN fill:#2196f3
    style LIVE fill:#ff9800
    style ERROR fill:#f44336
```

### Thermal Data Processing Pipeline

```mermaid
graph TD
    subgraph "Data Acquisition"
        HIK_DEV[HIK Device] --> RAW_HIK[Raw HIK Data]
        IR_DEV[IR Sensor] --> RAW_IR[Raw IR Data]
        BLE_DEV[BLE Thermal Tool] --> RAW_BLE[Raw BLE Data]
    end
    
    subgraph "Data Processing"
        RAW_HIK --> NORMALIZE[Data Normalization]
        RAW_IR --> NORMALIZE
        RAW_BLE --> NORMALIZE
        
        NORMALIZE --> CALIB[Calibration]
        CALIB --> FILTER[Noise Filtering]
        FILTER --> ENHANCE[Image Enhancement]
    end
    
    subgraph "Analysis & Visualization"
        ENHANCE --> TEMP_MAP[Temperature Mapping]
        TEMP_MAP --> PSEUDO[Pseudo-color Mapping]
        PSEUDO --> ANALYSIS[Temperature Analysis]
        
        ANALYSIS --> POINT[Point Analysis]
        ANALYSIS --> LINE[Line Analysis] 
        ANALYSIS --> AREA[Area Analysis]
        ANALYSIS --> POLY[Polygon Analysis]
    end
    
    subgraph "Output Generation"
        POINT --> REPORT[Report Generation]
        LINE --> REPORT
        AREA --> REPORT
        POLY --> REPORT
        
        PSEUDO --> DISPLAY[Live Display]
        PSEUDO --> SAVE_IMG[Save Image]
        PSEUDO --> EXPORT_3D[3D Export]
        
        REPORT --> PDF_OUT[PDF Report]
        SAVE_IMG --> GALLERY[Image Gallery]
        EXPORT_3D --> MODEL_3D[3D Model]
    end
    
    style HIK_DEV fill:#e3f2fd
    style IR_DEV fill:#e3f2fd
    style BLE_DEV fill:#e3f2fd
    style NORMALIZE fill:#f3e5f5
    style ANALYSIS fill:#e8f5e8
    style PDF_OUT fill:#fff3e0
```

### BLE Device Communication Flow

```mermaid
sequenceDiagram
    participant App as TopInfrared App
    participant BLE as BLE Module
    participant Device as Thermal Device
    participant UI as User Interface
    
    App->>BLE: Initialize BLE Scanner
    BLE->>BLE: Start device discovery
    BLE-->>App: Device found callback
    
    App->>UI: Show discovered devices
    UI->>App: User selects device
    
    App->>BLE: Connect to selected device
    BLE->>Device: Connection request
    Device-->>BLE: Connection response
    BLE-->>App: Connection established
    
    loop Data Streaming
        Device->>BLE: Thermal data packet
        BLE->>App: Raw thermal data
        App->>App: Process thermal data
        App->>UI: Update live thermal display
    end
    
    App->>BLE: Request device info
    BLE->>Device: Get device parameters
    Device-->>BLE: Device info response
    BLE-->>App: Device information
    
    App->>BLE: Send configuration
    BLE->>Device: Update device settings
    Device-->>BLE: Configuration confirmed
    
    Note over App,Device: Temperature measurement mode
    App->>BLE: Request temperature data
    BLE->>Device: Temperature measurement
    Device-->>BLE: Temperature readings
    BLE-->>App: Processed temperature data
    App->>UI: Display temperature analysis
    
    App->>BLE: Disconnect device
    BLE->>Device: Disconnection request
    Device-->>BLE: Disconnection confirmed
    BLE-->>App: Device disconnected
```

## 🔧 Setup and Installation

### Prerequisites
1. **Android Studio**: Arctic Fox (2020.3.1) or newer
2. **JDK**: Java 8 or Java 11
3. **Android SDK**: API Level 34
4. **NDK**: Version 21.3.6528147 (for native code compilation)
5. **Git**: For version control

### Development Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/buccancs/TopInfrared.git
   cd TopInfrared
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned TopInfrared directory
   - Wait for Gradle sync to complete

3. **Configure Build Environment**
   ```bash
   # Ensure you have the correct NDK version
   # In Android Studio: SDK Manager > SDK Tools > NDK (Side by side)
   # Select version 21.3.6528147
   ```

4. **Build the Project**
   ```bash
   ./gradlew build
   ```

5. **Run on Device/Emulator**
   ```bash
   # For development build
   ./gradlew installDevDebug
   
   # For production build
   ./gradlew installProdRelease
   ```

### Development Setup Flow

```mermaid
flowchart TD
    START([Start Setup]) --> PREREQ[Check Prerequisites]
    PREREQ --> AS{Android Studio Installed?}
    AS -->|No| INSTALL_AS[Install Android Studio]
    INSTALL_AS --> AS
    AS -->|Yes| JDK{JDK 8/11 Installed?}
    
    JDK -->|No| INSTALL_JDK[Install JDK]
    INSTALL_JDK --> JDK
    JDK -->|Yes| CLONE[Clone Repository]
    
    CLONE --> OPEN[Open in Android Studio]
    OPEN --> SYNC{Gradle Sync Success?}
    SYNC -->|No| DEPS[Install Dependencies]
    DEPS --> SYNC
    SYNC -->|Yes| NDK{NDK 21.3.6528147?}
    
    NDK -->|No| INSTALL_NDK[Install NDK via SDK Manager]
    INSTALL_NDK --> NDK
    NDK -->|Yes| BUILD[Build Project]
    
    BUILD --> SUCCESS{Build Success?}
    SUCCESS -->|No| DEBUG[Debug Build Issues]
    DEBUG --> BUILD
    SUCCESS -->|Yes| RUN[Run on Device]
    
    RUN --> COMPLETE([Setup Complete])
    
    style START fill:#4caf50
    style COMPLETE fill:#4caf50
    style BUILD fill:#2196f3
    style DEBUG fill:#ff9800
```

## 📦 Build Variants

TopInfrared supports multiple build variants to target different markets and Android versions:

### Build Variants Architecture

```mermaid
graph TB
    subgraph "Build Types"
        DEBUG[Debug]
        RELEASE[Release]
    end
    
    subgraph "Product Flavors"
        DEV[dev]
        BETA[beta] 
        PROD[prod]
        PRODTOP[prodTopdon]
    end
    
    subgraph "Generated Variants"
        DEBUG --> DEVDEBUG[devDebug]
        DEBUG --> BETADEBUG[betaDebug]
        DEBUG --> PRODDEBUG[prodDebug]
        
        RELEASE --> DEVREL[devRelease]
        RELEASE --> BETAREL[betaRelease]
        RELEASE --> PRODREL[prodRelease]
        RELEASE --> PRODTOPREL[prodTopdonRelease]
    end
    
    subgraph "Target Markets"
        PRODREL --> INTL[International Market]
        PRODTOPREL --> ANDROID10[Android 10 Devices]
    end
    
    style DEV fill:#4caf50
    style BETA fill:#ff9800
    style PROD fill:#2196f3
```

### Deployment Pipeline

```mermaid
flowchart LR
    subgraph "Source Code"
        CODE[Code Repository]
    end
    
    subgraph "Build Process"
        CODE --> BUILD{Build Type}
        BUILD -->|Debug| DEBUG_BUILD[Debug Build]
        BUILD -->|Release| RELEASE_BUILD[Release Build]
        
        DEBUG_BUILD --> DEV_APK[Development APK]
        RELEASE_BUILD --> SIGN[Code Signing]
        SIGN --> PROD_APK[Production APK]
        SIGN --> AAB[Android App Bundle]
    end
    
    subgraph "Distribution"
        DEV_APK --> TESTING[Internal Testing]
        PROD_APK --> STORE[App Stores]
        AAB --> PLAYSTORE[Google Play Store]
        
        TESTING --> FIREBASE[Firebase Distribution]
        STORE --> HUAWEI[Huawei AppGallery]
        STORE --> XIAOMI[Xiaomi GetApps]
        PLAYSTORE --> MARKETS[Global Markets]
    end
    
    style CODE fill:#e3f2fd
    style SIGN fill:#fff3e0
    style PLAYSTORE fill:#4caf50
    style MARKETS fill:#4caf50
```

TopInfrared supports multiple build variants to target different markets and Android versions:

### Flavor Dimensions

#### Development Flavors
- **`dev`**: Development build with debug features
- **`beta`**: Beta testing version
- **`prod`**: Production release for international markets
- **`prodTopdon`**: Production build for Android 10 compatibility

### Build Commands

```bash
# Debug builds
./gradlew assembleDevDebug          # Development debug
./gradlew assembleBetaDebug         # Beta debug
./gradlew assembleProdDebug         # Production debug

# Release builds  
./gradlew assembleDevRelease        # Development release
./gradlew assembleBetaRelease       # Beta release
./gradlew assembleProdRelease       # Production release
./gradlew assembleProdTopdonRelease # Android 10 production

# Generate AAB (Android App Bundle)
./gradlew bundleProdRelease
```

## 🛠️ Key Technologies

### Core Framework
- **Android SDK**: Target API 34, Minimum API 24
- **Kotlin**: Primary development language
- **Java**: Legacy code and third-party integrations
- **Gradle**: Build automation and dependency management

### Architecture Components
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Data Binding**: Two-way data binding for UI components
- **Room Database**: Local data persistence
- **RxJava**: Reactive programming for asynchronous operations

### Networking & Communication
- **Retrofit**: HTTP client for API communication
- **OkHttp**: Network layer and connection pooling
- **Bluetooth LE**: Device connectivity and communication
- **Firebase**: Analytics, crash reporting, and messaging

### Image Processing & Graphics
- **OpenCV**: Computer vision and image processing
- **JavaCV**: Java wrapper for OpenCV operations
- **Custom Matrix Operations**: Optimized thermal data processing
- **MPAndroidChart**: Data visualization and charting

### UI/UX Libraries
- **AndroidX**: Modern Android support libraries
- **Material Design**: Google's design system
- **Immersion Bar**: Status bar customization
- **XPopup**: Advanced popup components
- **SmartRefreshLayout**: Pull-to-refresh functionality

### Third-Party Integrations
- **Firebase Suite**: Analytics, Crashlytics, Cloud Messaging
- **WeChat SDK**: Social sharing integration
- **UMeng**: Analytics and A/B testing
- **Zoho SalesIQ**: Customer support integration

## 📁 Module Descriptions

### Core Modules

#### `app/` - Main Application
- Application entry point and main activity
- Global configuration and initialization
- Build variants and signing configurations
- Integration of all feature modules

#### `BleModule/` - Bluetooth Communication
- Bluetooth Low Energy device discovery and pairing
- Device communication protocols
- Connection management and error handling
- Hardware-specific communication adapters

#### `libir/` - Infrared Processing Core
- Core thermal imaging algorithms
- Temperature calibration and conversion
- Infrared sensor data processing
- Hardware abstraction layer

### Feature Modules

#### `component/thermal-*` - Device-Specific Integration
- **`thermal-hik/`**: HIK thermal camera integration
- **`thermal-ir/`**: Generic IR sensor support  
- **`thermal-lite/`**: Lightweight thermal processing
- **`thermal04/`** & **`thermal07/`**: Specific hardware models

#### `component/edit3d/` - 3D Visualization
- 3D thermal data representation
- Interactive 3D thermal models
- Advanced visualization controls

#### `component/pseudo/` - Color Processing
- Pseudo-color palette management
- Thermal-to-visible color mapping
- Custom color scheme creation

#### `component/transfer/` - Data Management
- File transfer utilities
- Data export/import functionality
- Cloud synchronization support

#### `component/user/` - Account Management
- User authentication and registration
- Profile management
- Settings and preferences

### Library Modules

#### `libcom/` - Common Utilities
- PDF report generation
- File system utilities
- Common helper functions
- Shared resources

#### `libui/` - UI Components
- Custom UI widgets
- Charting and graphing components
- Thermal image display components

#### `libhik/` - HIK Integration
- HIK-specific device drivers
- Protocol implementations
- Hardware communication layer

## 🧪 Testing

### Test Structure
```bash
# Unit Tests
./gradlew test

# Instrumented Tests
./gradlew connectedAndroidTest

# Specific module tests
./gradlew :app:test
./gradlew :BleModule:test
```

### Test Coverage
- Unit tests for core thermal processing algorithms
- Integration tests for Bluetooth communication
- UI tests for critical user workflows
- Hardware integration tests (requires physical devices)

## 📱 Deployment

### Release Build Process

1. **Prepare Release**
   ```bash
   # Update version in depend.gradle
   # Ensure all tests pass
   ./gradlew clean
   ```

2. **Generate Signed APK**
   ```bash
   ./gradlew assembleProdRelease
   ```

3. **Generate AAB for Play Store**
   ```bash
   ./gradlew bundleProdRelease
   ```

4. **Automated Build Scripts**
   ```bash
   # Google Play build
   ./build_release_google_apk_script.bat
   
   # TOPDON build
   ./build_release_topdon_apk_script.bat
   ```

### Distribution Channels
- **Google Play Store**: International distribution
- **TOPDON Store**: Regional distribution
- **Enterprise Distribution**: Direct APK distribution for business clients

## 🔒 Security & Privacy

### Data Protection
- Thermal measurement data encrypted at rest
- Secure user authentication
- Privacy-compliant data collection
- GDPR and regional privacy law compliance

### Permissions
- **Bluetooth**: Device connectivity
- **Camera**: Thermal camera access
- **Storage**: Thermal data and report storage
- **Internet**: Cloud features and analytics
- **Location**: Geo-tagging thermal measurements (optional)

## 👤 User Journey & Workflows

### Complete User Journey Map

```mermaid
journey
    title TopInfrared User Experience Journey
    section App Discovery & Setup
      Download App: 5: User
      Grant Permissions: 4: User
      Create Account: 4: User
      Setup Preferences: 3: User
    section Device Connection
      Scan for Devices: 3: User
      Pair Thermal Device: 4: User
      Calibrate Device: 3: User
      Test Connection: 5: User
    section Thermal Imaging
      Start Live Feed: 5: User
      Adjust Settings: 4: User
      Capture Image: 5: User
      Take Measurements: 5: User
    section Analysis & Reporting
      Analyze Temperature: 5: User
      Add Annotations: 4: User
      Generate Report: 5: User
      Export Data: 4: User
    section Data Management
      Browse Gallery: 4: User
      Organize Files: 3: User
      Share Results: 5: User
      Backup Data: 3: User
```

### Thermal Imaging Workflow

```mermaid
stateDiagram-v2
    [*] --> DeviceDiscovery
    
    DeviceDiscovery --> DeviceSelection: Device Found
    DeviceSelection --> Connection: User Selects Device
    Connection --> LiveFeed: Connection Success
    Connection --> DeviceDiscovery: Connection Failed
    
    LiveFeed --> Capturing: User Triggers Capture
    LiveFeed --> Settings: Adjust Parameters
    Settings --> LiveFeed: Apply Changes
    
    Capturing --> Processing: Image Captured
    Processing --> Analysis: Processing Complete
    
    Analysis --> Measurement: Add Temperature Points
    Analysis --> Annotation: Add Notes/Drawings
    Analysis --> Visualization: 3D View
    
    Measurement --> ReportGeneration
    Annotation --> ReportGeneration
    Visualization --> ReportGeneration
    
    ReportGeneration --> Gallery: Save to Gallery
    ReportGeneration --> Export: Export/Share
    ReportGeneration --> Print: Generate PDF
    
    Gallery --> Analysis: Re-analyze Image
    Export --> [*]
    Print --> [*]
    
    note right of LiveFeed : Real-time thermal feed\nwith measurement overlays
    note right of Analysis : Temperature analysis\nwith multiple measurement types
    note right of ReportGeneration : Professional PDF reports\nwith detailed analysis
```

## 🤝 Contributing

### Development Guidelines

1. **Code Style**
   - Follow Android Kotlin style guide
   - Use meaningful variable and function names
   - Maintain consistent indentation (4 spaces)
   - Add documentation for public APIs

2. **Commit Guidelines**
   ```bash
   # Format: type(scope): description
   feat(thermal): add new temperature calibration algorithm
   fix(ble): resolve connection timeout issues
   docs(readme): update installation instructions
   ```

3. **Pull Request Process**
   - Create feature branch from `main`
   - Ensure all tests pass
   - Update documentation if needed
   - Request review from maintainers

4. **Testing Requirements**
   - Add unit tests for new features
   - Verify existing tests still pass
   - Test on multiple Android versions
   - Hardware testing for device-specific features

### Development Environment Setup
```bash
# Install pre-commit hooks
git config core.hooksPath .githooks

# Set up development build
./gradlew assembleDevDebug
./gradlew installDevDebug
```

## 🐛 Troubleshooting

### Common Issues

#### Build Issues
```bash
# Clean and rebuild
./gradlew clean
./gradlew build

# Clear Gradle cache
rm -rf ~/.gradle/caches/
```

#### Bluetooth Connection Issues
- Ensure device permissions are granted
- Check if target hardware is in pairing mode
- Verify Android Bluetooth is enabled
- Try resetting Bluetooth cache in Android settings

#### Thermal Imaging Issues
- Verify camera hardware compatibility
- Check thermal sensor calibration
- Ensure adequate lighting conditions
- Update device firmware if available

### Debugging Tips
- Enable developer options on test device
- Use `adb logcat` for runtime debugging
- Check Firebase Crashlytics for production issues
- Utilize built-in thermal imaging debug mode

## 📞 Support & Contact

### Technical Support
- **Email**: support@topdon.com
- **Phone**: 1-833-629-4832
- **Website**: www.topdon.com
- **Address**: TOPDON USA INC., 400 Commons Way, Suite A, Rockaway, New Jersey 07866

### Development Team
- **Issues**: [GitHub Issues](https://github.com/buccancs/TopInfrared/issues)
- **Discussions**: [GitHub Discussions](https://github.com/buccancs/TopInfrared/discussions)
- **Documentation**: [Wiki](https://github.com/buccancs/TopInfrared/wiki)

### Community
- Professional thermal imaging community support
- Regular updates and feature releases
- Hardware compatibility updates
- User feedback integration

## 📄 License

This project includes various open source components licensed under different terms:

### Apache License 2.0 Components
- RxJava, Room, EventBus, MPAndroidChart
- Glide, Firebase, AndroidUtilCode
- XXPermissions, Gson, OkHttp, Retrofit
- XLog, javacv

### MIT License Components  
- SwipyRefreshLayout

### Proprietary Components
- Core thermal processing algorithms
- Hardware-specific device drivers
- Proprietary image processing enhancements

See `app/src/main/assets/web/third_statement.html` for complete license information.

## 🔄 Version History

### Current Version: v1.10.000 (Build 1100)
- Enhanced thermal image processing algorithms
- Improved Bluetooth connectivity stability
- New 3D thermal visualization features
- Updated UI/UX with Material Design 3
- Performance optimizations for low-end devices
- Extended hardware device compatibility

### Previous Releases
- v1.9.x: Major UI overhaul and cloud integration
- v1.8.x: Advanced thermal analysis tools
- v1.7.x: Multi-language support and localization
- v1.6.x: 3D thermal visualization introduction
- v1.5.x: Enhanced PDF reporting capabilities

---

**TopInfrared** - Empowering professional thermal imaging analysis on Android devices. 

*For the latest updates, visit our [GitHub repository](https://github.com/buccancs/TopInfrared) or contact our support team.*