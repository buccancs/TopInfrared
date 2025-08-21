# TC001 Enhanced Recording Features

This document describes the enhanced recording capabilities implemented in the TC001 standalone module, specifically the **Samsung 4K 30FPS recording** and **DNG RAW Level 3 recording** features.

## Overview

The TC001 standalone module now supports parallel advanced recording modes:

- **Samsung-specific 4K recording** at 30 FPS for Samsung devices
- **DNG RAW Level 3 recording** at 30 FPS for high-fidelity thermal data capture
- **Parallel recording** capability to run both modes simultaneously

## Samsung 4K 30FPS Recording

### Features
- **True 4K Resolution**: Records at 3840x2160 pixels for maximum thermal detail
- **30 FPS Frame Rate**: Smooth video capture optimized for thermal data
- **Samsung Optimization**: Uses Samsung-specific camera APIs and encoder settings
- **High Bitrate**: 20 Mbps encoding for professional thermal video quality
- **Device Detection**: Automatically detects Samsung devices and capabilities
- **Fallback Support**: Falls back to enhanced 1080p on non-4K capable devices

### Technical Implementation
```kotlin
// Samsung 4K recording configuration
MediaRecorder().apply {
    setVideoSource(MediaRecorder.VideoSource.SURFACE)
    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
    setVideoEncodingBitRate(20000000) // 20 Mbps for 4K
    setVideoFrameRate(30) // 30 FPS
    setVideoSize(3840, 2160) // True 4K resolution
}
```

### Usage
1. **Single Recording**: Tap "Samsung 4K" button to start/stop
2. **Status Display**: Real-time duration display during recording
3. **File Output**: Saves as `samsung_4k_TIMESTAMP.mp4` with metadata
4. **Device Requirements**: Samsung device with 4K recording support

## DNG RAW Level 3 Recording

### What is DNG RAW?
**DNG (Digital Negative) RAW** is Adobe's open standard for RAW image files, providing the highest fidelity thermal data capture and preservation. Level 3 processing offers maximum thermal data integrity for professional thermal analysis and scientific applications.

### Features
- **Stage 3/Level 3 Processing**: Highest quality thermal data capture
- **30 FPS RAW Capture**: Real-time thermal data recording at 30 frames per second
- **16-bit RAW Data**: Full thermal precision preservation in 16-bit format
- **Thermal Calibration**: Device-specific calibration matrix application  
- **Temperature Range**: -20°C to 150°C with 0.01°C precision
- **DNG 1.6 Standard**: Industry-standard RAW format for maximum compatibility
- **Metadata Preservation**: Complete thermal capture metadata in JSON format
- **Scientific Accuracy**: Calibrated thermal measurements for research applications

### Technical Implementation

#### DNG RAW Processor Architecture
```kotlin
class DngRawProcessor {
    // Level 3 constants
    private const val RAW_PROCESSING_FREQUENCY = 30 // 30 FPS
    private const val DNG_BITS_PER_SAMPLE = 16 // 16-bit RAW thermal data
    private const val THERMAL_PRECISION = 0.01f // High precision measurement
    
    // Thermal RAW data structure
    data class ThermalRawData(
        val timestamp: Long,
        val width: Int, val height: Int,
        val thermalData: Array<FloatArray>,
        val rawData: ByteArray,
        val metadata: ThermalMetadata
    )
}
```

#### Processing Pipeline
1. **Thermal Frame Acquisition**: 30 FPS thermal data capture
2. **Calibration Application**: Device-specific thermal calibration
3. **RAW Data Conversion**: 16-bit thermal data encoding
4. **DNG File Generation**: TIFF-based DNG file creation
5. **Metadata Embedding**: Comprehensive thermal metadata
6. **Quality Preservation**: Maximum thermal data integrity

### Scientific Applications
- **Research & Development**: High-fidelity thermal data preservation
- **Scientific Analysis**: Precise temperature measurements for research  
- **Calibration Studies**: Device characterization and validation
- **Archival Storage**: Long-term thermal data preservation in standard format

## Parallel Recording

### Capabilities
- **Simultaneous Recording**: Run Samsung 4K + DNG RAW Level 3 concurrently
- **Independent Controls**: Start/stop each recording type individually
- **Resource Management**: Optimized for multi-stream processing
- **Status Monitoring**: Real-time duration tracking for both streams
- **Automatic Cleanup**: Proper resource management and cleanup

### Usage Workflow
1. **Individual Recording**: Use "Samsung 4K" or "DNG RAW L3" buttons
2. **Parallel Recording**: Use "Parallel" button to start both simultaneously
3. **Status Monitoring**: Watch real-time duration counters
4. **Stopping**: Stop individual streams or all streams with "Stop All"

## UI Controls

### Button Layout
```
[Samsung 4K] [DNG RAW L3] [Parallel]
```

### Status Display
```
Samsung 4K: 05:23
DNG RAW L3: 05:23
```

### Recording States
- **Inactive**: Gray buttons, no status display
- **Active**: Red buttons, duration counters visible
- **Individual**: Single recording active
- **Parallel**: Both recordings active simultaneously

## File Output & Metadata

### Samsung 4K Output
- **Filename**: `samsung_4k_YYYYMMDD_HHMMSS.mp4`
- **Resolution**: 3840x2160 @ 30fps
- **Bitrate**: 20 Mbps
- **Metadata**: `samsung_4k_TIMESTAMP_metadata.json`

### DNG RAW Level 3 Output  
- **Filename**: `dng_raw_l3_YYYYMMDD_HHMMSS.mp4`
- **Resolution**: 1280x720 @ 30fps (optimized for thermal processing)
- **Bitrate**: 12 Mbps
- **Metadata**: `dng_raw_l3_TIMESTAMP_metadata.json`
- **DNG Files**: Individual `thermal_raw_TIMESTAMP.dng` files generated

### DNG RAW Metadata Structure
```json
{
    "recording_type": "DNG_RAW_Level3_30FPS",
    "analysis_level": 3,
    "stage": 3,
    "resolution": "1280x720",
    "frame_rate": 30,
    "duration_ms": 123456,
    "bitrate": "12Mbps",
    "thermal_device": "TC001",
    "dng_raw_processing": true,
    "thermal_fidelity": "high",
    "raw_format": "DNG_1.6",
    "timestamp": 1703123456789,
    "version": "2.0"
}
```

## Technical Architecture

### Enhanced Recording Manager
```
EnhancedRecordingManager
├── Samsung 4K Recording
│   ├── Device Detection
│   ├── 4K Profile Configuration  
│   ├── Samsung API Integration
│   └── High Bitrate Encoding
├── DNG RAW Level 3 Recording
│   ├── DngRawProcessor Integration
│   ├── 30 FPS Thermal Capture
│   ├── DNG File Generation
│   └── Thermal Metadata
└── Parallel Recording Management
    ├── Multi-stream Coordination
    ├── Resource Optimization
    ├── Status Monitoring
    └── Cleanup Management
```

### Integration with TC001ThermalManager
```kotlin
// Enhanced recording methods added to TC001ThermalManager
suspend fun startSamsung4KRecording(): Boolean
suspend fun startDngRawLevel3Recording(): Boolean  
suspend fun startParallelRecording(): Pair<Boolean, Boolean>
suspend fun stopEnhancedRecording(type: RecordingType): String?
fun isEnhancedRecordingActive(type: RecordingType): Boolean
```

## Performance Considerations

### System Requirements
- **Samsung 4K**: Samsung device with 4K recording capability
- **DNG RAW Level 3**: Minimum Android 7.0, 4GB RAM recommended
- **Parallel Recording**: 6GB RAM recommended for optimal performance

### Optimization Features
- **Automatic Quality Adjustment**: Falls back to lower resolution if needed
- **Memory Management**: Efficient cleanup and resource handling
- **Battery Optimization**: Configurable recording limits
- **Storage Management**: Automatic file size and duration limits

## Error Handling & Recovery

### Robust Error Management
- **Connection Monitoring**: Continuous device health checks
- **Automatic Recovery**: Reconnection logic with exponential backoff
- **Graceful Degradation**: Quality reduction on resource constraints
- **User Feedback**: Clear error messages and status updates

### Failure Scenarios
- **Device Disconnection**: Automatic pause and reconnect
- **Storage Full**: Automatic recording stop with notification
- **Memory Pressure**: Quality reduction and resource cleanup
- **Codec Failures**: Fallback to alternative encoders

This enhanced recording system provides professional-grade thermal video capture capabilities specifically optimized for TC001 device integration, making it suitable for scientific research, industrial applications, and advanced thermal analysis workflows.