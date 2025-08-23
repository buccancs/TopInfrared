package com.topdon.lib.menu

import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.elvishew.xlog.XLog
import com.example.opengl.IRSurfaceView
import com.example.opengl.render.IROpen3DTools
import com.example.opengl.render.IRRender
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.utils.ByteUtils.bytesToInt
import kotlinx.android.synthetic.main.activity_image_3d.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Route(path = RouterConfig.IR_GALLERY_3D)
class Image3DActivity : BaseActivity() {

    private var ir_path  : String ?= null
    private var temp_high : Float = 0F
    private var temp_low: Float = 0f
    private var tempX = 128
    private var tempY = 96

    private var open3DTools = IROpen3DTools()
    private var irRender : IRRender ?= null
    private var imageBytes: ByteArray = ByteArray(192 * 256 * 2)
    private val temperatureBytes = ByteArray(192*256*2)
    private lateinit var ir_sf : IRSurfaceView

    override fun initContentView(): Int {
        return R.layout.activity_image_3d
    }
    override fun initView() {
        ir_path = intent.getStringExtra(ExtraKeyConfig.IR_PATH).toString()
        temp_high = intent.getFloatExtra(ExtraKeyConfig.TEMP_HIGH,0f)
        temp_low = intent.getFloatExtra(ExtraKeyConfig.TEMP_LOW,0f)
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val file = File(ir_path)
                if (!file.exists()) {
                    XLog.w("IR文件不存在: ${file.absolutePath}")
                    return@withContext
                }
                XLog.w("IR文件: ${file.absolutePath}")
                val  allBytes = file.readBytes()
                val headLenBytes = ByteArray(2)
                System.arraycopy(allBytes, 0, headLenBytes, 0, 2)
                val headLen = headLenBytes.bytesToInt()
                System.arraycopy(allBytes, headLen, imageBytes, 0, imageBytes.size)
                System.arraycopy(allBytes, imageBytes.size+headLen, temperatureBytes, 0, temperatureBytes.size)
            }
            open3DTools.init(imageBytes!!,1)
            ir_sf = IRSurfaceView(this@Image3DActivity)
            irRender = IRRender(this@Image3DActivity)
            irRender?.initData(temp_high,temp_low,open3DTools)
            irRender?.changeMode(IRRender.TYPE_MODEL_EML)
            irRender?.visualAngle(IRRender.TYPE_3D)
            ir_frame.addView(ir_sf)
            ir_sf.setORenderer(irRender)
            ir_sf.setScaleView(1.3f)
            ir_sf.requestRender()
        }

        menu.onPseudoClickListener = {
            open3DTools.init(imageBytes!!,it+1)
            irRender?.updatePseudoModel(open3DTools)
            ir_sf.requestRender()
        }
        menu.onMarkClickListener = {
            when(it){
                0->{
                    irRender?.updatePointData(tempY,tempX,temp_high,temp_low,IROpen3DTools.TYPE_SEL_TEMP,temperatureBytes)
                    val temp = open3DTools.getRandomPoint(tempY,tempX,temp_high,temp_low,IROpen3DTools.TYPE_SEL_TEMP,temperatureBytes)
                    ir_sf.requestRender()
                    tv_temp.text = getXYZText(open3DTools.selTemp,temp[0][0],temp[0][1])
                    tv_temp.visibility = View.VISIBLE
                    bar_pick_view_x.visibility = View.VISIBLE
                    bar_pick_view_y.visibility = View.VISIBLE

                }
                1->{
                    irRender?.updatePointData(0,0,temp_high,temp_low,IROpen3DTools.TYPE_HIGHT_TEMP,temperatureBytes)
                    val temp = open3DTools.getRandomPoint(0,0,temp_high,temp_low,IROpen3DTools.TYPE_HIGHT_TEMP,temperatureBytes)
                    ir_sf.requestRender()
                    tv_temp.visibility = View.VISIBLE
                    tv_temp.text = getXYZText(temp_high,temp[0][0],temp[0][1])
                    bar_pick_view_x.visibility = View.GONE
                    bar_pick_view_y.visibility = View.GONE
                }
                2->{
                    irRender?.updatePointData(0,0,temp_high,temp_low,IROpen3DTools.TYPE_LOW_TEMP,temperatureBytes)
                    val temp = open3DTools.getRandomPoint(0,0,temp_high,temp_low,IROpen3DTools.TYPE_LOW_TEMP,temperatureBytes)
                    ir_sf.requestRender()
                    tv_temp.visibility = View.VISIBLE
                    tv_temp.text = getXYZText(temp_low,temp[0][0],temp[0][1])
                    bar_pick_view_x.visibility = View.GONE
                    bar_pick_view_y.visibility = View.GONE
                }
                3->{
                    irRender?.updatePointData(0,0,temp_high,temp_low,IROpen3DTools.TYPE_HIDE_TEMP,temperatureBytes)
                    ir_sf.requestRender()
                    tv_temp.visibility = View.GONE
                    bar_pick_view_x.visibility = View.GONE
                    bar_pick_view_y.visibility = View.GONE
                }
            }

        }
        menu.onModeClickListener = {
            when(it){
                0->{
                    irRender?.changeMode(IRRender.TYPE_MODEL_POINT)
                    ir_sf.requestRender()
                }
                1->{
                    irRender?.changeMode(IRRender.TYPE_MODEL_LINE)
                    ir_sf.requestRender()
                }
                2->{
                    irRender?.changeMode(IRRender.TYPE_MODEL_EML)
                    ir_sf.requestRender()
                }
            }
        }
        menu.onVisualClickListener = {
            when(it){
                0->{
                    irRender?.visualAngle(IRRender.TYPE_3D,true)
                    ir_sf.requestRender()
                }
                1->{
                    irRender?.visualAngle(IRRender.TYPE_TOP,true)
                    ir_sf.requestRender()
                }
                2->{
                    irRender?.visualAngle(IRRender.TYPE_LEFT,true)
                    ir_sf.requestRender()
                }
                3->{
                    irRender?.visualAngle(IRRender.TYPE_RIGHT,true)
                    ir_sf.requestRender()
                }
                4->{
                    irRender?.visualAngle(IRRender.TYPE_FRONT,true)
                    ir_sf.requestRender()
                }
            }
        }
        bar_pick_view_y.setProgressAndRefresh(tempY)
        bar_pick_view_x.setProgressAndRefresh(tempX)
        bar_pick_view_y.onProgressChanged = { progress, max ->
            tempY = max - progress

            try {
                irRender?.updatePointData(tempY, tempX, temp_high, temp_low, IROpen3DTools.TYPE_SEL_TEMP,temperatureBytes)
                val temp = open3DTools.getRandomPoint(tempY, tempX, temp_high, temp_low, IROpen3DTools.TYPE_SEL_TEMP,temperatureBytes)
                ir_sf.requestRender()
                tv_temp.text = getXYZText(open3DTools.selTemp, temp[0][0], temp[0][1])
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString() + tempX + "//" + tempY)
            }
        }
        bar_pick_view_x.onProgressChanged = { progress, max ->
            tempX = max - progress
            try {
                irRender?.updatePointData(tempY, tempX, temp_high, temp_low, IROpen3DTools.TYPE_SEL_TEMP,temperatureBytes)
                val temp = open3DTools.getRandomPoint(tempY, tempX, temp_high, temp_low, IROpen3DTools.TYPE_SEL_TEMP,temperatureBytes)
                ir_sf.requestRender()
                tv_temp.text = getXYZText(open3DTools.selTemp, temp[0][0], temp[0][1])
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString() + tempX + "//" + tempY)
            }
        }
    }

    private fun getXYZText(temp: Float, x : Float, y:Float):String{
        return "X ${(256 - (y * open3DTools.halfy + open3DTools.halfy)).toInt()}," +
                "Y ${( 192 - (x * open3DTools.halfx + open3DTools.halfx)).toInt()}," +
                "Z ${UnitTools.showC(temp)}"
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun initData() {
    }

}