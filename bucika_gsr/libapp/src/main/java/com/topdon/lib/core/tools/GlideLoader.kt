package com.topdon.lib.core.tools

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.topdon.lib.core.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 图片加载
 * Created by fylder on 2018/6/23.
 */
object GlideLoader {


    /**
     * 图像默认图
     */
    private fun getPhotoOptions(): RequestOptions {
        val multi = MultiTransformation(CenterCrop(), RoundedCorners(SizeUtils.dp2px(6f)))
        return RequestOptions
            .bitmapTransform(multi)
            .error(R.mipmap.ic_default_head)
    }

    /**
     * 圆形图片
     */
    fun loadCircle(img: ImageView, resourceId: Int, options: RequestOptions) {
        Glide.with(img)
            .load(resourceId)
            .apply(options)
            .into(img)
    }

    /**
     * 圆形图片
     */
    fun loadCircle(img: ImageView, url: String, options: RequestOptions) {
        Glide.with(img)
            .load(url)
            .apply(options)
            .into(img)
    }

    /**
     * 圆形图片
     */
    fun loadCircle(img: ImageView, drawable: Drawable, options: RequestOptions) {
        Glide.with(img)
            .load(drawable)
            .apply(options)
            .into(img)
    }

    /**
     * 圆形图片
     */
    fun loadCircle(img: ImageView, uri: Uri, options: RequestOptions) {
        Glide.with(img)
            .load(uri)
            .apply(options)
            .into(img)
    }

    /**
     * 圆形图片
     */
    fun loadCircle(img: ImageView, url: String, resourceId: Int, options: RequestOptions) {
        Glide.with(img)
            .load(url)
            .error(resourceId)
            .placeholder(resourceId)
            .apply(options)
            .into(img)
    }

    /**
     * 圆角形图片
     */
    fun loadRounded(img: ImageView, resourceId: Int) {
        Glide.with(img)
            .load(resourceId)
            .apply(getPhotoOptions())
            .into(img)
    }

    /**
     * 圆角形图片
     */
    fun loadRounded(img: ImageView, url: String) {
        Glide.with(img)
            .load(url)
            .apply(getPhotoOptions())
            .into(img)
    }

    /**
     * 圆角形图片
     */
    fun loadRounded(img: ImageView, drawable: Drawable) {
        Glide.with(img)
            .load(drawable)
            .apply(getPhotoOptions())
            .into(img)
    }

    /**
     * 圆角形图片
     */
    fun loadRounded(img: ImageView, uri: Uri) {
        Glide.with(img)
            .load(uri)
            .apply(getPhotoOptions())
            .into(img)
    }

    /**
     * 加载图片
     */
    fun load(img: ImageView, url: String?) {
        val multi = MultiTransformation(
            CenterCrop()
        )
        val options = RequestOptions
            .bitmapTransform(multi)
            .placeholder(R.mipmap.bg_default_img)
            .error(R.mipmap.bg_default_img)
        Glide.with(img)
            .load(url)
            .apply(options)
            .into(img)
    }

    /**
     * 加载图片
     */
    fun loadGallery(img: ImageView, url: String?) {
        val multi = MultiTransformation(
            CenterCrop()
        )
        val options = RequestOptions
            .bitmapTransform(multi)
            .placeholder(R.drawable.ic_gallery_default_shape)
            .error(R.drawable.ic_gallery_default_shape)
        Glide.with(img)
            .load(url)
            .apply(options)
            .into(img)
    }

    /**
     * 加载图片
     */
    fun loadFit(img: ImageView, url: String?) {
        val multi = MultiTransformation(
            FitCenter()
        )
        val options = RequestOptions
            .bitmapTransform(multi)
            .placeholder(R.drawable.ic_default_search_svg)
            .error(R.drawable.ic_default_search_svg)
        Glide.with(img)
            .load(url)
            .apply(options)
            .into(img)
    }

    /**
     * 加载图片
     */
    fun load(img: ImageView, resourceId: Int) {
        val multi = MultiTransformation(
            FitCenter()
        )
        val options = RequestOptions
            .bitmapTransform(multi)

        Glide.with(img)
            .load(resourceId)
            .apply(options)
            .into(img)
    }


    fun loadP(img: ImageView, url: String?) {
        Glide.with(img)
            .load(url)
            .placeholder(R.drawable.ic_default_search_svg)
            .into(img)
    }

    suspend fun getDrawable(context: Context, url: String?): Drawable? {
        if (url == null) {
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(context).asDrawable().load(url).submit().get()
            } catch (e: Exception) {
                null
            }
        }
    }
}