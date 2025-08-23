package com.topdon.house.activity

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.topdon.house.R
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.ktbase.BaseActivity
import kotlinx.android.synthetic.main.activity_images_detail.*

class ImagesDetailActivity : BaseActivity() {
    override fun initContentView(): Int = R.layout.activity_images_detail

    override fun initView() {
        val imageList: List<String> = intent.getStringArrayListExtra(ExtraKeyConfig.IMAGE_PATH_LIST) ?: return
        view_pager2.adapter = MyAdapter(imageList)
        view_pager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                title_view.setTitleText("${position + 1}/${imageList.size}")
            }
        })
        view_pager2.setCurrentItem(intent.getIntExtra(ExtraKeyConfig.CURRENT_ITEM, 0), false)
    }

    override fun initData() {
    }

    private class MyAdapter(private val imageList: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val imageView = ImageView(parent.context)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return ViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.imageView).load(imageList[position]).into(holder.imageView)
        }

        override fun getItemCount(): Int = imageList.size

        class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }
}