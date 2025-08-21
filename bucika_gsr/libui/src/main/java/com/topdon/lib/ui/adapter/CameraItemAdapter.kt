package com.topdon.lib.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.ui.R

/**
 * @author: CaiSongL
 * @date: 2023/4/1 13:48
 */
@Deprecated("热成像-菜单-拍照已重构，不需要这个类了")
class CameraItemAdapter(context: Context) : RecyclerView.Adapter<CameraItemAdapter.ViewHolder>() {

    val data: List<String> = listOf(
        context.getString(R.string.person_headshot_camera),
        context.getString(R.string.app_video)
    )

    private var parentRecycler: RecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        parentRecycler = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_weak, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.textView?.text = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val textView: TextView

        init {
            textView = itemView.findViewById<View>(R.id.name) as TextView
            itemView.findViewById<View>(R.id.container).setOnClickListener(this)
        }

        fun showText() {
            textView.pivotX = (textView.width / 2).toFloat()
            textView.pivotY = (textView.height / 2).toFloat()
            textView.setTextColor(0xffffffff.toInt())
            textView.animate().scaleX(1.1f)
                .withEndAction {
                    textView.setTextColor(0xffffffff.toInt())
                }
                .scaleY(1.1f).setDuration(100)
                .start()
        }

        fun hideText() {
            textView.setTextColor(ContextCompat.getColor(textView.context, R.color.ui_main_custom_color))
            //            textView.setColorFilter(ContextCompat.getColor(imageView.getContext(), R.color.Grey700));
            textView.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .start()
        }

        override fun onClick(v: View) {
            parentRecycler!!.smoothScrollToPosition(adapterPosition)
        }
    }
}