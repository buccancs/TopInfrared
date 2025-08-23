package com.topdon.module.thermal.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.maning.imagebrowserlibrary.ImageEngine
import com.topdon.module.thermal.R

class GlideImageEngine : ImageEngine {
    override fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        progressView: View,
        customImageView: View
    ) {
        val option = RequestOptions().centerCrop()

        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(option)
            .fitCenter()
            .placeholder(R.drawable.ic_default_head_svg)
            .error(R.drawable.ic_default_head_svg)
            .listener(BitmapRequestListener())
            .into(imageView)
    }

    class DrawableRequestListener : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }
    }

    class BitmapRequestListener : RequestListener<Bitmap> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Bitmap>?,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any?,
            target: Target<Bitmap>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

    }
}