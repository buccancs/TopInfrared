package com.topinfrared.tc001.common.constants

object TC001Constants {
    
    const val DEVICE_NAME = "TC001"
    const val DEVICE_TYPE = "Basic IR Camera"
    
    const val TC001_VENDOR_ID = 0x1234
    const val TC001_PRODUCT_ID = 0x5678
    
    const val THERMAL_WIDTH = 256
    const val THERMAL_HEIGHT = 192
    const val THERMAL_FPS = 10
    
    const val IMAGE_FORMAT = "jpg"
    const val VIDEO_FORMAT = "mp4"
    
    const val APP_FOLDER = "TC001_Thermal"
    const val IMAGES_FOLDER = "Images"
    const val VIDEOS_FOLDER = "Videos"
    
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    
    const val ACTION_USB_PERMISSION = "com.topinfrared.tc001.USB_PERMISSION"
    
    enum class TemperatureMode {
        POINT, LINE, AREA
    }
    
    const val IMAGE_NAME_PATTERN = "TC001_thermal_%s.jpg"
    const val VIDEO_NAME_PATTERN = "TC001_recording_%s.mp4"
    const val DATE_FORMAT = "yyyyMMdd_HHmmss"
}