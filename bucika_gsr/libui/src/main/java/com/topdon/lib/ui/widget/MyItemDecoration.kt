package com.topdon.lib.ui.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class MyItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    var wholeLeft: Float? = null
    
    var wholeRight: Float? = null
    
    var wholeTop: Float? = null
    
    var wholeBottom: Float? = null

    var itemLeft: Float? = null
    
    var itemRight: Float? = null
    
    var itemTop: Float? = null
    
    var itemBottom: Float? = null

    private val density: Float = context.resources.displayMetrics.density

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemCount = parent.adapter?.itemCount ?: return
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = parent.layoutManager

        when (layoutManager) {
            is GridLayoutManager -> {
                if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                    setVerticalMulti(outRect, position, itemCount, layoutManager.spanCount)
                } else {
                    setHorizontalMulti(outRect, position, itemCount, layoutManager.spanCount)
                }
            }
            is LinearLayoutManager -> {
                if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                    setVerticalOne(outRect, position, itemCount)
                } else {
                    setHorizontalOne(outRect, position, itemCount)
                }
            }
            is StaggeredGridLayoutManager -> {
                val layoutParams = view.layoutParams
                val spanIndex = if (layoutParams is StaggeredGridLayoutManager.LayoutParams) layoutParams.spanIndex else 0
                if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                    setVerticalMultiStaggered(outRect, position, itemCount, layoutManager.spanCount, spanIndex)
                } else {
                    setHorizontalMulti(outRect, position, itemCount, layoutManager.spanCount)
                }
            }
        }
    }

    private fun setVerticalOne(outRect: Rect, position: Int, itemCount: Int) {
        val left: Int = dp2px(wholeLeft ?: ((itemLeft ?: 0f) * 2))
        val right: Int = dp2px(wholeRight ?: ((itemRight ?: 0f) * 2))
        val top: Int = dp2px(if (position == 0) wholeTop ?: ((itemTop ?: 0f) * 2) else (itemTop ?: 0f))
        val bottom: Int = dp2px(if (position == itemCount - 1) wholeBottom ?: ((itemBottom ?: 0f) * 2) else (itemBottom ?: 0f))
        outRect.set(left, top, right, bottom)
    }

    private fun setHorizontalOne(outRect: Rect, position: Int, itemCount: Int) {
        val left: Int = dp2px(if (position == 0) wholeLeft ?: ((itemLeft ?: 0f) * 2) else (itemLeft ?: 0f))
        val right: Int = dp2px(if (position == itemCount - 1) wholeRight ?: ((itemRight ?: 0f) * 2) else (itemRight ?: 0f))
        val top: Int = dp2px(wholeTop ?: ((itemTop ?: 0f) * 2))
        val bottom: Int = dp2px(wholeBottom ?: ((itemBottom ?: 0f) * 2))
        outRect.set(left, top, right, bottom)
    }

    private fun setVerticalMulti(outRect: Rect, position: Int, itemCount: Int, spanCount: Int) {
        val totalRow = itemCount / spanCount + if (itemCount % spanCount == 0) 0 else 1
        val rowPosition = position / spanCount
        val columnPosition = position % spanCount

        val left: Int = dp2px(if (columnPosition == 0) wholeLeft ?: ((itemLeft ?: 0f) * 2) else (itemLeft ?: 0f))
        val right: Int = dp2px(if (columnPosition == spanCount - 1) wholeRight ?: ((itemRight ?: 0f) * 2) else (itemRight ?: 0f))
        val top: Int = dp2px(if (rowPosition == 0) wholeTop ?: ((itemTop ?: 0f) * 2) else (itemTop ?: 0f))
        val bottom: Int = dp2px(if (rowPosition == totalRow - 1) wholeBottom ?: ((itemBottom ?: 0f) * 2) else (itemBottom ?: 0f))
        outRect.set(left, top, right, bottom)
    }

    private fun setVerticalMultiStaggered(outRect: Rect, position: Int, itemCount: Int, spanCount: Int, spanIndex: Int) {
        val totalRow = itemCount / spanCount + if (itemCount % spanCount == 0) 0 else 1
        val rowPosition = position / spanCount

        val left: Int = dp2px(if (spanIndex == 0) wholeLeft ?: ((itemLeft ?: 0f) * 2) else (itemLeft ?: 0f))
        val right: Int = dp2px(if (spanIndex == spanCount - 1) wholeRight ?: ((itemRight ?: 0f) * 2) else (itemRight ?: 0f))
        val top: Int = dp2px(if (rowPosition == 0) wholeTop ?: ((itemTop ?: 0f) * 2) else (itemTop ?: 0f))
        val bottom: Int = dp2px(if (rowPosition == totalRow - 1) wholeBottom ?: ((itemBottom ?: 0f) * 2) else (itemBottom ?: 0f))
        outRect.set(left, top, right, bottom)
    }

    private fun setHorizontalMulti(outRect: Rect, position: Int, itemCount: Int, spanCount: Int) {
    }

    private fun dp2px(dpValue: Float): Int = (dpValue * density + 0.5f).toInt()
}