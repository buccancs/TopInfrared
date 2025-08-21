# TC001 Enhanced Recording Features

This document describes the enhanced recording capabilities implemented in the TC001 standalone module, specifically the **Samsung 4K 30FPS recording** and **RAD WND Level 3 recording** features.

## Overview

The TC001 standalone module now supports parallel advanced recording modes:

- **Samsung-specific 4K recording** at 30 FPS for Samsung devices
- **RAD WND (Radiance Wind) Level 3 recording** at 30 FPS for advanced thermal analysis
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

## RAD WND Level 3 Recording

### What is RAD WND?
**RAD WND (Radiance Wind)** is an advanced thermal imaging technique that analyzes radiance patterns in thermal data to detect wind effects and air flow dynamics. Level 3 processing provides the highest fidelity analysis for professional thermal applications.

### Features
- **Stage 3/Level 3 Processing**: Highest quality thermal wind analysis
- **30 FPS Analysis**: Real-time wind pattern detection at 30 frames per second
- **Wind Vector Calculation**: Computes wind direction, magnitude, and confidence
- **Thermal Gradient Analysis**: Advanced Sobel operator-based gradient calculation  
- **Radiance Variance Detection**: Statistical analysis of thermal patterns
- **Hot/Cold Spot Detection**: Automatic identification of temperature extremes
- **Temporal Analysis**: Wind pattern consistency tracking over time frames
- **High Precision**: 0.01 radiance precision for scientific applications

### Technical Implementation

#### RAD WND Processor Architecture
```kotlin
class RadWndProcessor {
    // Level 3 constants
    private const val L3_PROCESSING_FREQUENCY = 30 // 30 FPS
    private const val L3_WIND_VECTOR_RESOLUTION = 64 // High resolution wind vectors
    private const val L3_RADIANCE_PRECISION = 0.01f // High precision measurement
    
    // Wind vector data structure
    data class WindVector(
        val x: Float, val y: Float,
        val magnitude: Float,
        val direction: Float, // in degrees
        val confidence: Float
    )
}
```

#### Processing Pipeline
1. **Thermal Frame Acquisition**: 30 FPS thermal data capture
2. **Radiance Calculation**: Average radiance and variance analysis
3. **Gradient Computation**: Sobel operator for thermal gradients
4. **Wind Vector Analysis**: Block-based wind pattern detection
5. **Temporal Consistency**: Multi-frame wind pattern validation
6. **Metadata Generation**: Comprehensive analysis reports

### Scientific Applications
- **Environmental Monitoring**: Air flow pattern analysis
- **Building Performance**: HVAC efficiency assessment  
- **Industrial Applications**: Heat transfer optimization
- **Research & Development**: Advanced thermal dynamics study

## Parallel Recording

### Capabilities
- **Simultaneous Recording**: Run Samsung 4K + RAD WND Level 3 concurrently
- **Independent Controls**: Start/stop each recording type individually
- **Resource Management**: Optimized for multi-stream processing
- **Status Monitoring**: Real-time duration tracking for both streams
- **Automatic Cleanup**: Proper resource management and cleanup

### Usage Workflow
1. **Individual Recording**: Use "Samsung 4K" or "RAD WND L3" buttons
2. **Parallel Recording**: Use "Parallel" button to start both simultaneously
3. **Status Monitoring**: Watch real-time duration counters
4. **Stopping**: Stop individual streams or all streams with "Stop All"

## UI Controls

### Button Layout
```
[Samsung 4K] [RAD WND L3] [Parallel]
```

### Status Display
```
Samsung 4K: 05:23
RAD WND L3: 05:23
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

### RAD WND Level 3 Output  
- **Filename**: `rad_wnd_l3_YYYYMMDD_HHMMSS.mp4`
- **Resolution**: 1280x720 @ 30fps (optimized for thermal processing)
- **Bitrate**: 12 Mbps
- **Metadata**: `rad_wnd_l3_TIMESTAMP_metadata.json`

### Metadata Structure
```json
{
    "recording_type": "Samsung_4K_30FPS",
    "device": "Samsung Galaxy S23",
    "resolution": "3840x2160",
    "frame_rate": 30,
    "duration_ms": 123456,
    "bitrate": "20Mbps",
    "thermal_device": "TC001",
    "samsung_optimized": true,
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
├── RAD WND Level 3 Recording
│   ├── RadWndProcessor Integration
│   ├── 30 FPS Thermal Analysis
│   ├── Wind Vector Calculation
│   └── Metadata Generation
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
suspend fun startRadWndLevel3Recording(): Boolean  
suspend fun startParallelRecording(): Pair<Boolean, Boolean>
suspend fun stopEnhancedRecording(type: RecordingType): String?
fun isEnhancedRecordingActive(type: RecordingType): Boolean
```

## Performance Considerations

### System Requirements
- **Samsung 4K**: Samsung device with 4K recording capability
- **RAD WND Level 3**: Minimum Android 7.0, 4GB RAM recommended
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