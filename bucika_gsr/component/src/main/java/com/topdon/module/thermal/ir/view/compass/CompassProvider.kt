package com.topdon.module.thermal.ir.view.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.compass.FilterCompassWrapper
import com.kylecorry.andromeda.sense.compass.GravityCompensatedCompass
import com.kylecorry.andromeda.sense.compass.ICompass
import com.kylecorry.andromeda.sense.compass.LegacyCompass
import com.kylecorry.andromeda.sense.magnetometer.Magnetometer
import com.kylecorry.andromeda.sense.orientation.GeomagneticRotationSensor
import com.kylecorry.andromeda.sense.orientation.RotationSensor
import com.kylecorry.sol.math.filters.MovingAverageFilter

class CompassProvider(private val context: Context) {


    fun get(): ICompass {
        val smoothing = 1
        val useTrueNorth =  true

        var source =  CompassSource.RotationVector

        // Handle if the available sources have changed (not likely)
        val allSources = getAvailableSources(context)

        // There were no compass sensors found
        if (allSources.isEmpty()){
            return NullCompass()
        }

        if (!allSources.contains(source)) {
            source = allSources.firstOrNull() ?: CompassSource.CustomMagnetometer
        }

        val compass = when (source) {
            CompassSource.RotationVector -> {
                RotationSensor(context, useTrueNorth, SensorService.MOTION_SENSOR_DELAY)
            }

            CompassSource.GeomagneticRotationVector -> {
                GeomagneticRotationSensor(context, useTrueNorth, SensorService.MOTION_SENSOR_DELAY)
            }

            CompassSource.CustomMagnetometer -> {
                GravityCompensatedCompass(context, useTrueNorth, SensorService.MOTION_SENSOR_DELAY)
            }

            CompassSource.Orientation -> {
                LegacyCompass(context, useTrueNorth, SensorService.MOTION_SENSOR_DELAY)
            }
        }

        return MagQualityCompassWrapper(
            FilterCompassWrapper(
                compass,
                MovingAverageFilter((smoothing * 4).coerceAtLeast(1))
            ),
            Magnetometer(context, SensorManager.SENSOR_DELAY_NORMAL)
        )
    }

//    fun getOrientationSensor(): IOrientationSensor? {
//        // TODO: This isn't used by the actual orientation sensors (they should use it)
//        val useTrueNorth = prefs.useTrueNorth
//
//        var source = prefs.source
//
//        // Handle if the available sources have changed (not likely)
//        val allSources = getAvailableSources(context)
//
//        // There were no compass sensors found
//        if (allSources.isEmpty()){
//            return NullOrientationSensor()
//        }
//
//        if (!allSources.contains(source)) {
//            source = allSources.firstOrNull() ?: CompassSource.CustomMagnetometer
//        }
//
//        // TODO: Apply the smoothing / quality to the orientation sensor
//        if (source == CompassSource.RotationVector){
//            return RotationSensor(context, useTrueNorth, SensorService.MOTION_SENSOR_DELAY)
//        }
//
//        if (source == CompassSource.GeomagneticRotationVector){
//            return GeomagneticRotationSensor(context, useTrueNorth, SensorService.MOTION_SENSOR_DELAY)
//        }
//
//        // TODO: Construct this from existing sensors
//        return null
//    }

    companion object {
        /**
         * Returns the available compass sources in order of quality
         */
        fun getAvailableSources(context: Context): List<CompassSource> {
            val sources = mutableListOf<CompassSource>()

            if (Sensors.hasSensor(context, Sensor.TYPE_ROTATION_VECTOR)) {
                sources.add(CompassSource.RotationVector)
            }

            if (Sensors.hasSensor(context, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)) {
                sources.add(CompassSource.GeomagneticRotationVector)
            }

            if (Sensors.hasSensor(context, Sensor.TYPE_MAGNETIC_FIELD)) {
                sources.add(CompassSource.CustomMagnetometer)
            }

            @Suppress("DEPRECATION")
            if (Sensors.hasSensor(context, Sensor.TYPE_ORIENTATION)) {
                sources.add(CompassSource.Orientation)
            }

            return sources
        }
    }

}