package com.topdon.module.thermal.ir.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.ui.widget.MyItemDecoration
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.adapter.ConfigEmAdapter
import com.topdon.module.thermal.ir.bean.DataBean
import kotlinx.android.synthetic.main.dialog_config_guide.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 温度修正操作指引.
 *
 * Created by LCG on 2024/11/13.
 */
class ConfigGuideDialog(context: Context, val isTC007: Boolean, val dataBean: DataBean) : Dialog(context, R.style.TransparentDialog) {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_config_guide, null))

        tv_default_temp_title.text = "${context.getString(R.string.thermal_config_environment)} ${UnitTools.showConfigC(-10, if (isTC007) 50 else 55)}"
        tv_default_dis_title.text = "${context.getString(R.string.thermal_config_distance)} (0.2~${if (isTC007) 4 else 5}m)"
        tv_space_em_title.text = "${context.getString(R.string.thermal_config_radiation)} (${if (isTC007) "0.1" else "0.01"}~1.00)"

        tv_default_em_title.text = "${context.getString(R.string.thermal_config_radiation)} (${if (isTC007) "0.1" else "0.01"}~1.00)"
        tv_default_em_value.text = NumberTools.to02(dataBean.radiation)


        val itemDecoration = MyItemDecoration(context)
        itemDecoration.wholeBottom = 20f

        recycler_view.addItemDecoration(itemDecoration)
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = ConfigEmAdapter(context)

        cl_step1.isVisible = SharedManager.configGuideStep == 1
        cl_step2_top.isVisible = SharedManager.configGuideStep == 2
        cl_step2_bottom.isVisible = SharedManager.configGuideStep == 2

        tv_next.setOnClickListener {
            cl_step1.isVisible = false
            cl_step2_top.isVisible = true
            cl_step2_bottom.isVisible = true
            SharedManager.configGuideStep = 2
        }
        tv_i_know.setOnClickListener {
            dismiss()
            SharedManager.configGuideStep = 0
        }
    }

    fun blurBg(rootView: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sourceBitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
                val outputBitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(sourceBitmap)
                rootView.draw(canvas)

                val renderScript = RenderScript.create(context)
                val inputAllocation = Allocation.createFromBitmap(renderScript, sourceBitmap)
                val outputAllocation = Allocation.createTyped(renderScript, inputAllocation.type)

                val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
                blurScript.setRadius(20f)
                blurScript.setInput(inputAllocation)
                blurScript.forEach(outputAllocation)
                outputAllocation.copyTo(outputBitmap)
                renderScript.destroy()

                launch(Dispatchers.Main) {
                    iv_blur_bg.isVisible = true
                    iv_blur_bg.setImageBitmap(outputBitmap)
                }
            } catch (_: Exception) {

            }
        }
    }
}