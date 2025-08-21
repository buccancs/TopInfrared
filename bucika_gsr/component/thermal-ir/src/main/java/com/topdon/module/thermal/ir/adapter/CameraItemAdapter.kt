package com.topdon.module.thermal.ir.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.ui.listener.SingleClickListener
import com.topdon.lib.ui.widget.CountDownView
import com.topdon.module.thermal.ir.R

/**
 * @author: CaiSongL
 * @date: 2023/4/3 10:18
 */
class CameraItemAdapter(
    data: MutableList<CameraItemBean>? = null) : BaseQuickAdapter<CameraItemBean, BaseViewHolder>(R.layout.item_camera,data) {


    var listener: ((index: Int,item: CameraItemBean) -> Unit)? = null

    override fun convert(holder: BaseViewHolder, item: CameraItemBean) {
        holder.setVisible(R.id.img,true)
        holder.setGone(R.id.count_down_view,true)
        holder?.itemView?.setOnClickListener (object :SingleClickListener(){
            override fun onSingleClick() {
                listener?.invoke(data.indexOf(item),item)
            }
        })
        when (item.type) {
            CameraItemBean.TYPE_DELAY -> {
                holder.setImageResource(R.id.img,R.drawable.svg_camera_delay_0)
                if (CameraItemBean.DELAY_TIME_0 == item.time){
                    holder.setVisible(R.id.img,true)
                    holder.setGone(R.id.count_down_view,true)
                }else{
                    holder.setVisible(R.id.img,false)
                    holder.setGone(R.id.count_down_view,false)
                    val countDownView = holder.getView<CountDownView>(R.id.count_down_view)
                    holder.setGone(R.id.count_down_view,false)
                    countDownView.setCountdownTime(item.time)
                }
            }
            CameraItemBean.TYPE_ZDKM -> {
                holder.setImageResource(R.id.img,if (item.isSel) R.drawable.svg_camera_auto_select_yes
                else R.drawable.svg_camera_auto_select_not)
            }
            CameraItemBean.TYPE_SDKM -> {
                holder.setImageResource(R.id.img,if (item.isSel) R.drawable.svg_camera_shutter_select_yes
                else R.drawable.svg_camera_shutter_select_not )
            }
            CameraItemBean.TYPE_AUDIO -> {
                holder.setImageResource(R.id.img,if (item.isSel) R.drawable.svg_camera_audio_select_yes
                else R.drawable.svg_camera_audio_select_not )
            }
            else -> {
                holder.setImageResource(R.id.img,R.drawable.svg_camera_setting)
            }
        }
    }

}