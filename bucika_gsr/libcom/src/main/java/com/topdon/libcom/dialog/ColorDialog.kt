package com.topdon.libcom.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorListener
import com.topdon.libcom.R
import com.topdon.libcom.adpter.DColorSelectAdapter

/**
 * 调色板
 * @author: CaiSongL
 * @date: 2023/4/23 16:07
 */
@Deprecated("产品要求所有颜色拾取都更改为 ColorPickDialog 那种样式，这个弹框废弃")
class ColorDialog(color: Int) : DialogFragment() {


    var positiveEvent  : ((color: Int)->Unit)? = null
    var cancelEvent: (() -> Unit)? = null

    var selColor : Int = color

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_color_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cyView =  view.findViewById<RecyclerView>(R.id.color_picker_recycler)
        val pView = view.findViewById<ColorPickerView>(R.id.color_picker_view)
        val colorAdapter = DColorSelectAdapter(requireContext())
        for (tmp in colorAdapter.colorBean){
            if (Color.parseColor(tmp.color) == selColor){
                colorAdapter.selected(colorAdapter.colorBean.indexOf(tmp))
                break
            }
        }
        cyView.layoutManager = GridLayoutManager(context, colorAdapter.itemCount)
        cyView.adapter = colorAdapter
        pView.setInitialColor(selColor)
        pView.setColorListener(object : ColorListener{
            override fun onColorSelected(color: Int, fromUser: Boolean) {
                if (fromUser){
                    selColor = color
                    colorAdapter.selected(-1)
                }
            }
        })
        colorAdapter.listener = { _, color ->
            selColor = color
        }
        view.findViewById<View>(R.id.dialog_tip_success_btn).setOnClickListener {
            dismiss()
            positiveEvent?.invoke(selColor)
        }
        view.findViewById<View>(R.id.dialog_tip_cancel_btn).setOnClickListener {
            dismiss()
        }
    }


    override fun onDestroy() {
        cancelEvent?.invoke()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}