package com.topdon.menu.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.topdon.menu.R
import com.topdon.menu.databinding.ViewCameraMenuBinding

class CameraMenuView : FrameLayout, View.OnClickListener {
    companion object {
        
        const val CODE_ACTION = 0
        
        const val CODE_GALLERY = 1
        
        const val CODE_MORE = 2
        
        const val CODE_TO_PHOTO = 3
        
        const val CODE_TO_VIDEO = 4
    }

    var isVideoMode: Boolean
        get() = binding.viewPager2.currentItem == 1
        set(value) {
            binding.viewPager2.currentItem = if (value) 1 else 0
        }

    var canSwitchMode: Boolean
        get() = binding.viewPager2.isUserInputEnabled
        set(value) {
            binding.viewPager2.isUserInputEnabled = value
            binding.tvPhoto.isVisible = value
            binding.tvVideo.isVisible = value
        }

    var onCameraClickListener: ((actionCode: Int) -> Unit)? = null

    fun setToNormal() {
        if (isVideoMode) {
            binding.ivAction.setImageResource(R.drawable.svg_camera_video_normal)
        } else {
            binding.ivAction.setImageResource(R.drawable.svg_camera_photo_normal)
        }
    }

    fun setToRecord(isDelay: Boolean) {
        if (isVideoMode) {
            binding.ivAction.setImageResource(R.drawable.svg_camera_video_record)
        } else {
            if (isDelay) {
                binding.ivAction.setImageResource(R.drawable.svg_camera_photo_record_delay)
            } else {
                binding.ivAction.setImageResource(R.drawable.svg_camera_photo_record_at_once)
            }
        }
    }

    fun refreshGallery(path: String) {
        try {
            Glide.with(this)
                .load(path)
                .apply(
                    RequestOptions.bitmapTransform(MultiTransformation(CenterCrop()))
                        .placeholder(R.drawable.shape_oval_33)
                        .error(R.drawable.shape_oval_33)
                )
                .into(binding.ivGallery)
        } catch (_: Exception) {
        }
    }

    private lateinit var binding: ViewCameraMenuBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        if (isInEditMode) {
            LayoutInflater.from(context).inflate(R.layout.view_camera_menu, this, true)
        } else {
            binding = ViewCameraMenuBinding.inflate(LayoutInflater.from(context), this, true)

            binding.viewPager2.adapter = MenuCameraAdapter()
            binding.viewPager2.registerOnPageChangeCallback(MyOnPageChangeCallback())

            binding.ivAction.setOnClickListener(this)
            binding.ivGallery.setOnClickListener(this)
            binding.ivMore.setOnClickListener(this)
            binding.tvPhoto.setOnClickListener(this)
            binding.tvVideo.setOnClickListener(this)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.viewPager2.dispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        when (v) {
            binding.ivAction -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 500) {
                    lastClickTime = currentTime
                    onCameraClickListener?.invoke(CODE_ACTION)
                }
            }
            binding.ivGallery -> {
                onCameraClickListener?.invoke(CODE_GALLERY)
            }
            binding.ivMore -> {
                onCameraClickListener?.invoke(CODE_MORE)
            }
            binding.tvPhoto -> {
                binding.viewPager2.currentItem = 0
            }
            binding.tvVideo -> {
                binding.viewPager2.currentItem = 1
            }
        }
    }

    inner class MyOnPageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val scrollWidth: Float = binding.tvPhoto.width / 2f + binding.tvVideo.width / 2f
            if (position == 0) {
                binding.tvPhoto.translationX = -scrollWidth * positionOffset
                binding.tvVideo.translationX = -scrollWidth * positionOffset
            } else {
                binding.tvPhoto.translationX = -scrollWidth
                binding.tvVideo.translationX = -scrollWidth
            }
        }

        override fun onPageSelected(position: Int) {
            binding.tvPhoto.isSelected = position == 0
            binding.tvVideo.isSelected = position == 1
            binding.ivAction.setImageResource(if (position == 1) R.drawable.svg_camera_video_normal else R.drawable.svg_camera_photo_normal)
            onCameraClickListener?.invoke(if (position == 1) CODE_TO_VIDEO else CODE_TO_PHOTO)
        }
    }

    class MenuCameraAdapter : RecyclerView.Adapter<MenuCameraAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = View(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        }

        override fun getItemCount(): Int = 2

        class ViewHolder(rootView: View): RecyclerView.ViewHolder(rootView)
    }
}