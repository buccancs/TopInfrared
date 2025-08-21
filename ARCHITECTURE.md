# TopInfrared Architecture Documentation

This document provides a comprehensive overview of TopInfrared's technical architecture, design patterns, and system components.

## 🏛️ High-Level Architecture

TopInfrared follows a modular Android architecture based on Clean Architecture principles with clear separation between presentation, domain, and data layers.

### Clean Architecture Overview

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[UI Components]
        VM[ViewModels]
        VIEWS[Activities & Fragments]
    end
    
    subgraph "Domain Layer"
        UC[Use Cases]
        REPO_INT[Repository Interfaces]
        MODELS[Domain Models]
        BUSINESS[Business Logic]
    end
    
    subgraph "Data Layer"
        REPO_IMPL[Repository Implementations]
        
        subgraph "Data Sources"
            REMOTE[Remote Data Source]
            LOCAL[Local Data Source]
            HARDWARE[Hardware Abstraction]
        end
        
        subgraph "External"
            API[REST APIs]
            DB[Room Database]
            BLE[BLE Devices]
            HIK[HIK SDK]
            FIREBASE[Firebase]
        end
    end
    
    UI --> VM
    VM --> UC
    UC --> REPO_INT
    REPO_INT --> REPO_IMPL
    REPO_IMPL --> REMOTE
    REPO_IMPL --> LOCAL
    REPO_IMPL --> HARDWARE
    
    REMOTE --> API
    REMOTE --> FIREBASE
    LOCAL --> DB
    HARDWARE --> BLE
    HARDWARE --> HIK
    
    UC --> BUSINESS
    BUSINESS --> MODELS
    
    style UI fill:#e3f2fd
    style UC fill:#f3e5f5
    style REPO_IMPL fill:#e8f5e8
    style API fill:#fff3e0
    style DB fill:#fff3e0
    style BLE fill:#fff3e0
```

### Data Flow Architecture

```mermaid
flowchart TD
    subgraph "External Inputs"
        THERMAL[Thermal Devices]
        USER[User Input]
        CLOUD[Cloud Services]
    end
    
    subgraph "Hardware Abstraction Layer"
        HIK_HAL[HIK Abstraction]
        IR_HAL[IR Abstraction]
        BLE_HAL[BLE Abstraction]
    end
    
    subgraph "Data Processing Pipeline"
        RAW[Raw Data Ingestion]
        VALIDATE[Data Validation]
        TRANSFORM[Data Transformation]
        PROCESS[Thermal Processing]
        CACHE[Data Caching]
    end
    
    subgraph "Business Logic Layer"
        TEMP_CALC[Temperature Calculation]
        IMAGE_PROC[Image Processing]
        ANALYSIS[Thermal Analysis]
        REPORT_GEN[Report Generation]
    end
    
    subgraph "Presentation Layer"
        LIVE_VIEW[Live View]
        GALLERY[Image Gallery]
        ANALYTICS[Analytics Dashboard]
        EXPORT[Export Functions]
    end
    
    subgraph "Storage Layer"
        DB_LOCAL[Local Database]
        FILE_SYS[File System]
        CLOUD_STORE[Cloud Storage]
    end
    
    THERMAL --> HIK_HAL
    THERMAL --> IR_HAL
    THERMAL --> BLE_HAL
    
    HIK_HAL --> RAW
    IR_HAL --> RAW
    BLE_HAL --> RAW
    USER --> RAW
    CLOUD --> RAW
    
    RAW --> VALIDATE
    VALIDATE --> TRANSFORM
    TRANSFORM --> PROCESS
    PROCESS --> CACHE
    
    CACHE --> TEMP_CALC
    CACHE --> IMAGE_PROC
    TEMP_CALC --> ANALYSIS
    IMAGE_PROC --> ANALYSIS
    ANALYSIS --> REPORT_GEN
    
    ANALYSIS --> LIVE_VIEW
    REPORT_GEN --> GALLERY
    ANALYSIS --> ANALYTICS
    REPORT_GEN --> EXPORT
    
    CACHE --> DB_LOCAL
    IMAGE_PROC --> FILE_SYS
    REPORT_GEN --> FILE_SYS
    EXPORT --> CLOUD_STORE
    
    style THERMAL fill:#ffeb3b
    style RAW fill:#ff9800
    style PROCESS fill:#2196f3
    style ANALYSIS fill:#4caf50
    style LIVE_VIEW fill:#9c27b0
```

## 📦 Module Architecture

### Core Application Module (`app/`)

The main application module serves as the entry point and orchestrates all feature modules.

#### Responsibilities:
- Application lifecycle management
- Dependency injection setup (Dagger/Hilt)
- Navigation coordination between modules
- Global configuration and theming
- Build variant management

#### Key Components:
```kotlin
// Application class with dependency injection
class TopInfraredApplication : Application(), HasAndroidInjector {
    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>
    
    override fun onCreate() {
        super.onCreate()
        DaggerApplicationComponent.create().inject(this)
    }
}

// Main activity with navigation host
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    // Handle navigation between feature modules
}
```

### Feature Modules

#### Thermal Processing Modules

##### `component/thermal-ir/` - Core Thermal Processing
```kotlin
// Core thermal data processing
interface ThermalProcessor {
    fun processRawData(data: ByteArray): ThermalImage
    fun applyCalibration(image: ThermalImage): ThermalImage
    fun convertToTemperature(rawValue: Int): Float
}

// Temperature measurement tools
class TemperatureMeasurement {
    fun measurePoint(x: Int, y: Int): Temperature
    fun measureArea(polygon: List<Point>): TemperatureStats
    fun measureLine(start: Point, end: Point): List<Temperature>
}
```

##### `component/thermal-hik/` - HIK Device Integration
Specialized module for HIK thermal camera communication:
- Custom communication protocols
- Device-specific calibration algorithms
- Hardware abstraction layer

##### `component/pseudo/` - Color Processing
```kotlin
// Thermal-to-visible color mapping
class PseudoColorProcessor {
    fun applyColorPalette(thermalImage: ThermalImage, palette: ColorPalette): Bitmap
    fun createCustomPalette(colors: List<Color>): ColorPalette
}

enum class ColorPalette {
    IRON, RAINBOW, GRAYSCALE, HOT, COOL, MEDICAL
}
```

##### `component/edit3d/` - 3D Visualization
Advanced 3D thermal visualization using OpenGL ES:
```kotlin
class Thermal3DRenderer : GLSurfaceView.Renderer {
    fun renderThermalMesh(thermalData: Array<Array<Float>>)
    fun applyLighting(lightPosition: Vector3)
    fun handleUserInteraction(gesture: MotionEvent)
}
```

#### Connectivity Modules

##### `BleModule/` - Bluetooth Low Energy
Handles all Bluetooth device communication:

```kotlin
// BLE device management
interface BleDeviceManager {
    fun scanForDevices(): Observable<BleDevice>
    fun connectToDevice(device: BleDevice): Single<Connection>
    fun writeCharacteristic(char: UUID, data: ByteArray): Completable
    fun readCharacteristic(char: UUID): Single<ByteArray>
}

// Connection state management
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Error : ConnectionState()
}
```

##### Hardware Abstraction Layer
```kotlin
// Generic thermal device interface
interface ThermalDevice {
    val deviceInfo: DeviceInfo
    val capabilities: List<DeviceCapability>
    
    suspend fun initialize(): Result<Unit>
    suspend fun startStreaming(): Flow<ThermalFrame>
    suspend fun stopStreaming()
    suspend fun captureImage(): ThermalImage
}

// Specific device implementations
class HikThermalDevice : ThermalDevice { /* ... */ }
class GenericBtThermalDevice : ThermalDevice { /* ... */ }
```

### Library Modules

#### `libcom/` - Common Utilities

##### PDF Generation
```kotlin
object PDFGenerator {
    fun generateThermalReport(
        thermalImages: List<ThermalImage>,
        measurements: List<Measurement>,
        template: ReportTemplate
    ): File {
        // Generate professional thermal analysis reports
    }
}
```

##### File Management
```kotlin
class FileManager {
    fun saveThermalImage(image: ThermalImage): File
    fun exportMeasurementData(data: List<Measurement>): File
    fun createBackup(): File
}
```

#### `libui/` - UI Components

Custom UI components optimized for thermal imaging:

```kotlin
// Thermal image display with measurement overlay
class ThermalImageView : ImageView {
    fun setThermalImage(image: ThermalImage)
    fun addMeasurementOverlay(measurement: Measurement)
    fun setColorPalette(palette: ColorPalette)
}

// Temperature scale bar
class TemperatureScaleView : View {
    fun setTemperatureRange(min: Float, max: Float)
    fun setUnit(unit: TemperatureUnit)
}
```

#### `libir/` - Infrared Processing Core

Core infrared and thermal processing algorithms:

```kotlin
// Low-level thermal processing
object ThermalAlgorithms {
    fun calibrateRawData(raw: IntArray, calibration: CalibrationData): FloatArray
    fun applyNonUniformityCorrection(data: FloatArray): FloatArray
    fun temperatureToRaw(temp: Float, calibration: CalibrationData): Int
}

// Image enhancement
object ImageEnhancement {
    fun sharpen(image: ThermalImage): ThermalImage
    fun denoise(image: ThermalImage): ThermalImage
    fun enhanceContrast(image: ThermalImage): ThermalImage
}
```

## 🔄 Data Flow Architecture

### Thermal Image Processing Pipeline

```mermaid
flowchart TD
    RAW[Raw Sensor Data] --> CAPTURE[Data Capture]
    CAPTURE --> CALIB[Calibration]
    CALIB --> PROC[Processing]
    PROC --> TEMP[Temperature Conversion]
    TEMP --> VIZ[Visualization]
    VIZ --> OUTPUT[Display/Export]
    
    subgraph "Hardware Abstraction Layer"
        CAPTURE
        HIK_HAL[HIK Hardware]
        IR_HAL[IR Hardware]
        BLE_HAL[BLE Hardware]
    end
    
    subgraph "Data Processing Pipeline"
        CALIB
        PROC
        TEMP
        
        CALIB --> CAL_DATA[Apply Device-Specific Calibration]
        PROC --> NUC[Non-Uniformity Correction]
        PROC --> FILTER[Filtering & Enhancement]
        TEMP --> TEMP_CALC[Temperature Calculation]
    end
    
    subgraph "Visualization Pipeline"
        VIZ
        PALETTE[Color Palette Application]
        OVERLAY[Measurement Overlays]
        UI_COMP[UI Components]
        
        VIZ --> PALETTE
        PALETTE --> OVERLAY
        OVERLAY --> UI_COMP
    end
    
    subgraph "Output Destinations"
        OUTPUT
        DISPLAY[Live Display]
        SAVE[Save to Gallery]
        EXPORT[Export Functions]
        PDF[PDF Reports]
        
        OUTPUT --> DISPLAY
        OUTPUT --> SAVE
        OUTPUT --> EXPORT
        OUTPUT --> PDF
    end
    
    HIK_HAL --> CAPTURE
    IR_HAL --> CAPTURE
    BLE_HAL --> CAPTURE
    
    style RAW fill:#ffeb3b
    style CAPTURE fill:#ff9800
    style CALIB fill:#2196f3
    style TEMP fill:#4caf50
    style VIZ fill:#9c27b0
    style OUTPUT fill:#f44336
```

### Component Interaction Diagram

```mermaid
graph TB
    subgraph "Feature Modules Layer"
        TH[thermal-hik]
        TIR[thermal-ir]
        TL[thermal-lite]
        E3D[edit3d]
        PSEUDO[pseudo]
        USER[user]
    end
    
    subgraph "Service Layer"
        BLE_SERVICE[BLE Communication Service]
        DATA_SERVICE[Data Processing Service]
        FILE_SERVICE[File Management Service]
        REPORT_SERVICE[Report Generation Service]
    end
    
    subgraph "Repository Layer"
        THERMAL_REPO[Thermal Repository]
        USER_REPO[User Repository]
        DEVICE_REPO[Device Repository]
        IMAGE_REPO[Image Repository]
    end
    
    subgraph "Data Sources"
        ROOM_DB[Room Database]
        FILE_SYSTEM[File System]
        SHARED_PREFS[Shared Preferences]
        CLOUD[Firebase/Cloud]
    end
    
    TH --> DATA_SERVICE
    TIR --> DATA_SERVICE
    TL --> DATA_SERVICE
    E3D --> FILE_SERVICE
    PSEUDO --> DATA_SERVICE
    USER --> USER_REPO
    
    TH --> BLE_SERVICE
    TIR --> BLE_SERVICE
    
    DATA_SERVICE --> THERMAL_REPO
    FILE_SERVICE --> IMAGE_REPO
    REPORT_SERVICE --> IMAGE_REPO
    BLE_SERVICE --> DEVICE_REPO
    
    THERMAL_REPO --> ROOM_DB
    THERMAL_REPO --> FILE_SYSTEM
    USER_REPO --> SHARED_PREFS
    USER_REPO --> CLOUD
    DEVICE_REPO --> SHARED_PREFS
    IMAGE_REPO --> FILE_SYSTEM
    
    style TH fill:#e3f2fd
    style DATA_SERVICE fill:#f3e5f5
    style THERMAL_REPO fill:#e8f5e8
    style ROOM_DB fill:#fff3e0
```

### Reactive Programming with RxJava

```kotlin
class ThermalDataProcessor {
    fun processThermalStream(): Observable<ThermalImage> {
        return thermalDevice.getDataStream()
            .map { rawData -> applyCalibration(rawData) }
            .map { calibratedData -> processImage(calibratedData) }
            .map { processedData -> convertToImage(processedData) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
    }
}
```

## 🗄️ Data Persistence

### Room Database Architecture

```kotlin
// Core entities
@Entity(tableName = "thermal_images")
data class ThermalImageEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val deviceId: String,
    val filePath: String,
    val temperature: ThermalData
)

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val imageId: String,
    val type: MeasurementType,
    val coordinates: String,
    val temperature: Float,
    val timestamp: Long
)

// Data Access Objects
@Dao
interface ThermalImageDao {
    @Query("SELECT * FROM thermal_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<ThermalImageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ThermalImageEntity)
    
    @Query("DELETE FROM thermal_images WHERE timestamp < :cutoff")
    suspend fun deleteOldImages(cutoff: Long)
}
```

## 🗄️ Data Persistence Architecture

### Database Schema Visualization

```mermaid
erDiagram
    THERMAL_IMAGES {
        string id PK
        long timestamp
        string deviceId FK
        string filePath
        json thermalData
        string userId FK
        float minTemp
        float maxTemp
        string colorPalette
    }
    
    MEASUREMENTS {
        string id PK
        string imageId FK
        string type
        json coordinates
        float temperature
        long timestamp
        string notes
    }
    
    DEVICES {
        string id PK
        string name
        string type
        json calibrationData
        boolean isConnected
        long lastSeen
    }
    
    USERS {
        string id PK
        string username
        string email
        json preferences
        long createdAt
        long lastLogin
    }
    
    REPORTS {
        string id PK
        string imageId FK
        string filePath
        string template
        json metadata
        long generatedAt
    }
    
    THERMAL_IMAGES }|--|| DEVICES : "captured_with"
    THERMAL_IMAGES ||--o{ MEASUREMENTS : "contains"
    THERMAL_IMAGES }|--|| USERS : "belongs_to"
    THERMAL_IMAGES ||--o{ REPORTS : "generates"
```

### File System Architecture

```mermaid
graph TD
    ROOT[/Android/data/com.topdon.tc001/files/]
    
    subgraph "User Data"
        ROOT --> THERMAL[ThermalImages/]
        THERMAL --> USER_DIR[{userid}/]
        USER_DIR --> GALLERY[Gallery/]
        USER_DIR --> DATALOG[DataLog/]
        USER_DIR --> PDF[Pdf/]
    end
    
    subgraph "System Data"
        ROOT --> CACHE[Cache/]
        ROOT --> BACKUP[Backup/]
        ROOT --> CONFIG[Config/]
    end
    
    subgraph "File Types"
        GALLERY --> IMG_RAW[.thermal files]
        GALLERY --> IMG_JPEG[.jpg preview]
        DATALOG --> CSV[.csv data]
        DATALOG --> JSON[.json metadata]
        PDF --> REPORTS[.pdf reports]
        CACHE --> TEMP[Temporary processing]
        BACKUP --> SYNC[Sync data]
        CONFIG --> SETTINGS[App settings]
    end
    
    subgraph "Access Patterns"
        IMG_RAW --> READ_FAST[Fast Read Access]
        CSV --> APPEND[Append-Only Logs]
        REPORTS --> SHARE[Shareable Format]
        TEMP --> AUTO_CLEANUP[Auto Cleanup]
    end
    
    style ROOT fill:#e3f2fd
    style USER_DIR fill:#f3e5f5
    style CACHE fill:#fff3e0
    style REPORTS fill:#e8f5e8
```

### Data Flow in Storage Layer

```mermaid
sequenceDiagram
    participant App as Application
    participant Repo as Repository
    participant Room as Room DB
    participant FileSystem as File System
    participant Cloud as Cloud Storage
    
    App->>Repo: Save thermal image
    Repo->>FileSystem: Store image file
    FileSystem-->>Repo: File path
    Repo->>Room: Save metadata
    Room-->>Repo: Database ID
    
    par Background Sync
        Repo->>Cloud: Upload metadata
        Repo->>Cloud: Upload image (if enabled)
    end
    
    Repo-->>App: Save complete
    
    Note over App,Cloud: Query Flow
    App->>Repo: Get thermal images
    Repo->>Room: Query metadata
    Room-->>Repo: Image metadata list
    
    loop For each image
        Repo->>FileSystem: Load thumbnail
        FileSystem-->>Repo: Thumbnail data
    end
    
    Repo-->>App: Complete image list
```

## 🔧 Build System Architecture

### Multi-Flavor Build Configuration

```gradle
// Product flavors for different markets
productFlavors {
    dev {
        buildConfigField("String", "API_BASE_URL", "\"https://dev-api.topdon.com\"")
        buildConfigField("boolean", "DEBUG_MODE", "true")
    }
    
    prod {
        buildConfigField("String", "API_BASE_URL", "\"https://api.topdon.com\"")
        buildConfigField("boolean", "DEBUG_MODE", "false")
    }
    
    insideChina {
        buildConfigField("String", "API_BASE_URL", "\"https://cn-api.topdon.com\"")
        // China-specific configurations
    }
}

// Feature-specific build configurations
dependencies {
    // Flavor-specific dependencies
    prodImplementation 'com.google.firebase:firebase-analytics'
    insideChinaImplementation 'com.umeng.umsdk:analytics'
    
    // Debug tools only in development
    debugImplementation 'com.squareup.leakcanary:leakcanary-android'
}
```

### Native Module Integration (NDK)

```cmake
# CMakeLists.txt for native thermal processing
cmake_minimum_required(VERSION 3.10.2)
project("thermal-native")

# OpenCV integration
find_package(OpenCV REQUIRED)

# Thermal processing library
add_library(thermal-native SHARED
    thermal_processor.cpp
    calibration.cpp
    image_enhancement.cpp
)

target_link_libraries(thermal-native
    ${OpenCV_LIBS}
    android
    log
)
```

## 🔐 Security Architecture

### Security Layers Overview

```mermaid
graph TB
    subgraph "Application Layer Security"
        APP_AUTH[User Authentication]
        APP_PERM[Permission Management]
        APP_VALIDATE[Input Validation]
        APP_CRYPTO[Data Encryption]
    end
    
    subgraph "Network Layer Security"
        TLS[TLS 1.3]
        CERT_PIN[Certificate Pinning]
        API_AUTH[API Authentication]
        FIREWALL[Network Firewall]
    end
    
    subgraph "Device Layer Security"
        KEYSTORE[Android Keystore]
        BIO_AUTH[Biometric Auth]
        SECURE_STORAGE[Secure Storage]
        ROOT_DETECT[Root Detection]
    end
    
    subgraph "Data Layer Security"
        DB_ENCRYPT[Database Encryption]
        FILE_ENCRYPT[File Encryption]
        BACKUP_SECURE[Secure Backup]
        DATA_MASKING[Data Masking]
    end
    
    APP_AUTH --> BIO_AUTH
    APP_CRYPTO --> KEYSTORE
    APP_AUTH --> API_AUTH
    API_AUTH --> TLS
    TLS --> CERT_PIN
    
    KEYSTORE --> SECURE_STORAGE
    SECURE_STORAGE --> DB_ENCRYPT
    SECURE_STORAGE --> FILE_ENCRYPT
    
    style APP_AUTH fill:#4caf50
    style KEYSTORE fill:#2196f3
    style TLS fill:#ff9800
    style DB_ENCRYPT fill:#9c27b0
```

### Authentication Flow

```mermaid
sequenceDiagram
    participant User as User
    participant App as TopInfrared App
    participant Bio as Biometric System
    participant Keystore as Android Keystore
    participant Server as Auth Server
    
    User->>App: Launch Application
    App->>App: Check authentication state
    
    alt First Launch
        App->>User: Setup biometric authentication
        User->>Bio: Register fingerprint/face
        Bio-->>App: Biometric registered
        App->>Keystore: Generate authentication key
        Keystore-->>App: Key generated
    end
    
    App->>User: Request authentication
    User->>Bio: Provide biometric
    Bio->>Keystore: Unlock authentication key
    Keystore-->>App: Authentication key
    
    App->>Server: Authenticate with token
    Server-->>App: Authentication response
    
    alt Success
        App->>User: Grant access to thermal features
    else Failure
        App->>User: Authentication failed
        App->>App: Lock sensitive features
    end
```

### Data Encryption Architecture

```mermaid
graph TD
    subgraph "Encryption Keys"
        MASTER_KEY[Master Key - Keystore]
        DEK[Data Encryption Keys]
        KEK[Key Encryption Key]
    end
    
    subgraph "Encrypted Data Types"
        THERMAL_DATA[Thermal Image Data]
        USER_DATA[User Credentials]
        DEVICE_CONFIG[Device Configuration]
        MEASUREMENT_DATA[Temperature Measurements]
    end
    
    subgraph "Encryption Methods"
        AES_256[AES-256-GCM]
        RSA_2048[RSA-2048]
        PBKDF2[PBKDF2]
    end
    
    subgraph "Storage Locations"
        SECURE_DB[Encrypted Database]
        SECURE_FILES[Encrypted Files]
        KEYSTORE_STORAGE[Android Keystore]
    end
    
    MASTER_KEY --> KEK
    KEK --> DEK
    DEK --> AES_256
    
    THERMAL_DATA --> AES_256
    USER_DATA --> RSA_2048
    DEVICE_CONFIG --> AES_256
    MEASUREMENT_DATA --> AES_256
    
    AES_256 --> SECURE_DB
    AES_256 --> SECURE_FILES
    RSA_2048 --> KEYSTORE_STORAGE
    
    style MASTER_KEY fill:#4caf50
    style AES_256 fill:#2196f3
    style SECURE_DB fill:#ff9800
```

### Network Security Implementation

```mermaid
flowchart LR
    subgraph "Client Side"
        APP[TopInfrared App]
        CERT_STORE[Certificate Store]
        PIN_CONFIG[Certificate Pins]
    end
    
    subgraph "Network Layer"
        TLS_HANDSHAKE[TLS Handshake]
        CERT_VALIDATION[Certificate Validation]
        PINNING_CHECK[Certificate Pinning Check]
    end
    
    subgraph "Server Side"
        API_SERVER[API Server]
        SSL_CERT[SSL Certificate]
        FIREWALL[WAF/Firewall]
    end
    
    APP --> TLS_HANDSHAKE
    TLS_HANDSHAKE --> CERT_VALIDATION
    CERT_VALIDATION --> CERT_STORE
    CERT_VALIDATION --> PINNING_CHECK
    PINNING_CHECK --> PIN_CONFIG
    
    TLS_HANDSHAKE <--> SSL_CERT
    SSL_CERT --> API_SERVER
    FIREWALL --> API_SERVER
    
    TLS_HANDSHAKE -.->|"Reject if pinning fails"| APP
    PINNING_CHECK -.->|"Validate against known pins"| PIN_CONFIG
    
    style APP fill:#e3f2fd
    style PINNING_CHECK fill:#fff3e0
    style FIREWALL fill:#ffebee
```

## 📊 Performance Optimization

### Memory Management

```kotlin
// Efficient bitmap handling for thermal images
class ThermalBitmapManager {
    private val bitmapCache = LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 8).toInt()
    )
    
    fun loadThermalImage(path: String): Bitmap? {
        // Implement efficient bitmap loading with caching
        // Use BitmapFactory.Options for memory optimization
    }
}

// Background processing optimization
class ThermalProcessingScheduler {
    fun scheduleProcessing(task: ProcessingTask) {
        when (task.priority) {
            Priority.HIGH -> Schedulers.immediate().schedule(task)
            Priority.LOW -> Schedulers.computation().schedule(task)
        }
    }
}
```

### Thermal Data Compression

```kotlin
// Lossless compression for thermal data
object ThermalDataCompression {
    fun compress(thermalData: FloatArray): ByteArray {
        // Custom compression algorithm optimized for thermal data
        // Maintain temperature accuracy while reducing file size
    }
    
    fun decompress(compressedData: ByteArray): FloatArray {
        // Decompress thermal data for processing
    }
}
```

## 🧪 Testing Architecture

### Unit Testing Strategy

```kotlin
// Repository testing with mocks
@RunWith(MockitoJUnitRunner::class)
class ThermalRepositoryTest {
    @Mock private lateinit var thermalDao: ThermalDao
    @Mock private lateinit var deviceManager: ThermalDeviceManager
    
    @InjectMocks private lateinit var repository: ThermalRepository
    
    @Test
    fun `should save thermal image successfully`() {
        // Test thermal data persistence
    }
}

// Hardware abstraction layer testing
class MockThermalDevice : ThermalDevice {
    override suspend fun captureImage(): ThermalImage {
        // Return mock thermal data for testing
    }
}
```

### Integration Testing

```kotlin
// End-to-end thermal processing pipeline testing
@RunWith(AndroidJUnit4::class)
class ThermalProcessingIntegrationTest {
    @Test
    fun `should process thermal data from device to display`() {
        // Test complete thermal imaging workflow
    }
}
```

## 🚀 Deployment Architecture

### Continuous Integration

```yaml
# GitHub Actions workflow
name: Build and Test
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run unit tests
        run: ./gradlew test
      - name: Build APK
        run: ./gradlew assembleProdRelease
```

## 🔄 Complete System Architecture Overview

### Comprehensive System Diagram

```mermaid
graph TB
    subgraph "User Interface Layer"
        UI[UI Components]
        VM[ViewModels]
        ACTIVITIES[Activities & Fragments]
    end
    
    subgraph "Business Logic Layer"
        UC_THERMAL[Thermal Use Cases]
        UC_USER[User Use Cases]
        UC_DEVICE[Device Use Cases]
        UC_REPORT[Report Use Cases]
    end
    
    subgraph "Feature Modules"
        THERMAL_HIK[thermal-hik]
        THERMAL_IR[thermal-ir]
        EDIT3D[edit3d]
        PSEUDO[pseudo]
        USER_MODULE[user]
    end
    
    subgraph "Service Layer"
        BLE_SERVICE[BLE Service]
        DATA_SERVICE[Data Processing Service]
        FILE_SERVICE[File Management Service]
        REPORT_SERVICE[Report Generation Service]
        SYNC_SERVICE[Cloud Sync Service]
    end
    
    subgraph "Repository Layer"
        THERMAL_REPO[Thermal Repository]
        USER_REPO[User Repository]
        DEVICE_REPO[Device Repository]
        IMAGE_REPO[Image Repository]
    end
    
    subgraph "Data Sources"
        ROOM_DB[(Room Database)]
        FILE_SYSTEM[File System]
        SHARED_PREFS[SharedPreferences]
        FIREBASE[Firebase]
    end
    
    subgraph "Hardware Layer"
        HIK_DEVICE[HIK Thermal Camera]
        IR_SENSOR[IR Sensors]
        BLE_THERMAL[BLE Thermal Tools]
    end
    
    subgraph "External Services"
        FIREBASE_ANALYTICS[Firebase Analytics]
        FIREBASE_CRASHLYTICS[Firebase Crashlytics]
        CLOUD_STORAGE[Cloud Storage]
        API_SERVER[API Server]
    end
    
    UI --> VM
    VM --> ACTIVITIES
    ACTIVITIES --> UC_THERMAL
    ACTIVITIES --> UC_USER
    ACTIVITIES --> UC_DEVICE
    ACTIVITIES --> UC_REPORT
    
    UC_THERMAL --> THERMAL_HIK
    UC_THERMAL --> THERMAL_IR
    UC_USER --> USER_MODULE
    UC_REPORT --> EDIT3D
    UC_THERMAL --> PSEUDO
    
    THERMAL_HIK --> BLE_SERVICE
    THERMAL_IR --> DATA_SERVICE
    USER_MODULE --> SYNC_SERVICE
    EDIT3D --> FILE_SERVICE
    PSEUDO --> REPORT_SERVICE
    
    BLE_SERVICE --> DEVICE_REPO
    DATA_SERVICE --> THERMAL_REPO
    FILE_SERVICE --> IMAGE_REPO
    SYNC_SERVICE --> USER_REPO
    
    THERMAL_REPO --> ROOM_DB
    THERMAL_REPO --> FILE_SYSTEM
    USER_REPO --> SHARED_PREFS
    USER_REPO --> FIREBASE
    DEVICE_REPO --> SHARED_PREFS
    IMAGE_REPO --> FILE_SYSTEM
    
    BLE_SERVICE --> HIK_DEVICE
    BLE_SERVICE --> IR_SENSOR
    BLE_SERVICE --> BLE_THERMAL
    
    SYNC_SERVICE --> FIREBASE_ANALYTICS
    SYNC_SERVICE --> FIREBASE_CRASHLYTICS
    FILE_SERVICE --> CLOUD_STORAGE
    UC_USER --> API_SERVER
    
    style UI fill:#e3f2fd
    style UC_THERMAL fill:#f3e5f5
    style THERMAL_REPO fill:#e8f5e8
    style HIK_DEVICE fill:#fff3e0
    style FIREBASE fill:#ffebee
```

### Technology Stack Visualization

```mermaid
mindmap
  root((TopInfrared
  Technology Stack))
    Android Platform
      Android SDK 34
      Kotlin & Java
      Android Architecture Components
      Material Design 3
    Hardware Integration
      HIK SDK
      IR Sensors
      Bluetooth LE
      USB Host API
      NDK/Native Code
    Data & Storage
      Room Database
      SharedPreferences
      File System
      SQLite
      JSON Processing
    Networking
      OkHttp
      Retrofit
      Firebase SDK
      TLS/SSL
      Certificate Pinning
    Image Processing
      OpenCV
      Custom Thermal Algorithms
      Bitmap Processing
      3D Rendering
      Color Space Conversion
    UI & UX
      Jetpack Compose
      Custom Views
      Charts & Graphs
      3D Visualization
      Multi-language Support
    Testing & Quality
      JUnit & Mockito
      Espresso
      Firebase Test Lab
      Static Analysis
      Code Coverage
    Build & DevOps
      Gradle
      Multi-flavor Builds
      ProGuard/R8
      GitHub Actions
      Firebase Distribution
```

This comprehensive architecture documentation provides detailed insights into TopInfrared's modular design, data flow patterns, security implementations, and technology stack. The visual diagrams help developers understand the complex interactions between different system components and facilitate easier maintenance and future development.

### Release Pipeline

```bash
# Automated release process
./gradlew assembleProdRelease        # Build release APK
./gradlew bundleProdRelease          # Build AAB for Play Store
./gradlew publishProdReleaseApk      # Publish to distribution channels
```

This architecture ensures:
- **Scalability**: Modular design supports easy feature additions
- **Maintainability**: Clear separation of concerns and testable components
- **Performance**: Optimized for thermal data processing and visualization
- **Security**: Secure handling of thermal measurement data
- **Reliability**: Robust error handling and state management

---

For more detailed implementation examples, check the source code in the respective modules.