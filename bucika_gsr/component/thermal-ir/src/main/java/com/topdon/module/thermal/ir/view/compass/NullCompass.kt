package com.topdon.module.thermal.ir.view.compass

import com.kylecorry.andromeda.sense.compass.ICompass
import com.kylecorry.sol.units.Bearing

class NullCompass : NullSensor(), ICompass {
    override val bearing: Bearing = Bearing(0f)

    override var declination: Float = 0f

    override val rawBearing: Float = 0f
}