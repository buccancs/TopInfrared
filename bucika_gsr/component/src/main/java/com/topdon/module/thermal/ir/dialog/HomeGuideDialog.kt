package com.topdon.module.thermal.ir.dialog

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
import com.topdon.module.thermal.ir.R
import kotlinx.android.synthetic.main.dialog_home_guide.*
import kotlinx.android.synthetic.main.layout_home_guide_1.*
import kotlinx.android.synthetic.main.layout_home_guide_2.*
import kotlinx.android.synthetic.main.layout_home_guide_3.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 首页操作指引弹框.
 *
 * Created by LCG on 2024/4/8.
 */
class HomeGuideDialog(context: Context, private val currentStep: Int) : Dialog(context, R.style.TransparentDialog) {

    /**
     * 下一步点击事件监听，step：当前处于第`[1,3]`，在该步骤点击的下一步
     */
    var onNextClickListener: ((step: Int) -> Unit)? = null

    /**
     * 跳过点击事件监听.
     */
    var onSkinClickListener: (() -> Unit)? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_home_guide, null))

        when (currentStep) {
            1 -> {
                cl_guide_1.isVisible = true
                cl_guide_2.isVisible = false
                cl_guide_3.isVisible = false
            }
            2 -> {
                cl_guide_1.isVisible = false
                cl_guide_2.isVisible = true
                cl_guide_3.isVisible = false
            }
            3 -> {
                cl_guide_1.isVisible = false
                cl_guide_2.isVisible = false
                cl_guide_3.isVisible = true
            }
        }

        tv_next1.setOnClickListener {
            onNextClickListener?.invoke(1)
            cl_guide_1.isVisible = false
            cl_guide_2.isVisible = true
        }
        tv_next2.setOnClickListener {
            onNextClickListener?.invoke(2)
            cl_guide_2.isVisible = false
            cl_guide_3.isVisible = true
        }
        tv_i_know.setOnClickListener {
            onNextClickListener?.invoke(3)
            dismiss()
        }


        tv_skin1.setOnClickListener {
            onSkinClickListener?.invoke()
            dismiss()
        }
        tv_skin2.setOnClickListener {
            onSkinClickListener?.invoke()
            dismiss()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onSkinClickListener?.invoke()
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