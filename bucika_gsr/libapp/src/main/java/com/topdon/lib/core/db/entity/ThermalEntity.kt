package com.topdon.lib.core.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.topdon.lib.core.tools.TimeTool

@Entity(tableName = "thermal")
class ThermalEntity  {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "thermal_id")
    var thermalId: String = ""

    @ColumnInfo(name = "user_id")
    var userId: String = ""

    @ColumnInfo(name = "thermal")
    var thermal: Float = 0f

    @ColumnInfo(name = "thermal_max")
    var thermalMax: Float = 0f

    @ColumnInfo(name = "thermal_min")
    var thermalMin: Float = 0f

    @ColumnInfo(name = "sn")
    var sn: String = ""

    @ColumnInfo(name = "info")
    var info: String = ""

    @ColumnInfo(name = "type")
    var type: String = ""

    @ColumnInfo(name = "start_time")
    var startTime: Long = 0

    @ColumnInfo(name = "create_time")
    var createTime: Long = 0

    //单位ms
    @ColumnInfo(name = "update_time")
    var updateTime: Long = 0

    override fun toString(): String {
        return "ThermalEntity(id=$id, thermalId='$thermalId', userId='$userId', thermal=$thermal, thermalMax=$thermalMax, thermalMin=$thermalMin, sn='$sn', info='$info', type='$type', startTime=$startTime, createTime=$createTime, updateTime=$updateTime)"
    }

    fun getTime(): String {
        return TimeTool.reportTime(createTime)
    }

    fun getMaxTemp(): Float {
        return thermalMax
    }

    fun getMinTemp(): Float {
        return thermalMin
    }

}
