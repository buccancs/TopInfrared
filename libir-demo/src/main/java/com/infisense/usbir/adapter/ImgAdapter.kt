package com.infisense.usbir.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.infisense.usbir.R
import com.infisense.usbir.bean.ImgBean
import kotlinx.android.synthetic.main.item_filter.view.*

class ImgAdapter(
    private val context: Context,
    private val mDataList: ArrayList<ImgBean>,
    var listenter: OnItemOnClickListenter
) : RecyclerView.Adapter<ImgAdapter.ViewHolder>() {

    private var bitmap: Bitmap? = null

    interface OnItemOnClickListenter {
        fun onClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false)
        return ViewHolder(view)
    }

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        notifyDataSetChanged()
    }

    fun imageScale(bitmap: Bitmap, dst_w: Int, dst_h: Int): Bitmap {
        val src_w = bitmap.width
        val src_h = bitmap.height
        val scale_w = dst_w.toFloat() / src_w
        val scale_h = dst_h.toFloat() / src_h
        val matrix = Matrix()
        matrix.postScale(scale_w, scale_h)
        return Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true)
    }

    var canvas: Canvas? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterBean = mDataList[position]
        holder.tvName.text = filterBean.titleName
        holder.filter_img.setImageResource(filterBean.img)
        holder.rlRoot.setOnClickListener {
            listenter.onClick(
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textureView: TextureView = view.textureView
        var tvName: TextView = view.filter_name
        var filter_img: ImageView = view.filter_img
        var rlRoot: View = view.rl_root
    }
}
