package com.topdon.module.thermal.ir.view.compass

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.compass.ICompass
import com.topdon.module.thermal.ir.view.compass.CompassProvider

class SensorService(ctx: Context) {

    private var context = ctx.applicationContext

    fun hasCompass(): Boolean {
        return Sensors.hasCompass(context)
    }

    fun getCompass(): ICompass {
        return CompassProvider(context).get()
    }

    companion object {
        const val MOTION_SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME
        const val ENVIRONMENT_SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL
    }

}