package com.topdon.lib.ui.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * RecyclerView 所用，控制间距.
 *
 * Created by LCG on 2023/9/20.
 */
class MyItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    /**
     * 整个 RecyclerView 左侧间距，单位 dp.
     * 该值仅对最左侧 item 有效，且与 [itemLeft] 不叠加（优先使用当前值）.
     */
    var wholeLeft: Float? = null
    /**
     * 整个 RecyclerView 右侧间距，单位 dp.
     * 该值仅对最右侧 item 有效，且与 [itemRight] 不叠加（优先使用当前值）.
     */
    var wholeRight: Float? = null
    /**
     * 整个 RecyclerView 顶部间距，单位 dp.
     * 该值仅对最顶部 item 有效，且与 [itemTop] 不叠加（优先使用当前值）.
     */
    var wholeTop: Float? = null
    /**
     * 整个 RecyclerView 底部间距，单位 dp.
     * 该值仅对最底部 item 有效，且与 [itemBottom] 不叠加（优先使用当前值）.
     */
    var wholeBottom: Float? = null


    /**
     * 每个 item 左侧间距，单位 dp.
     */
    var itemLeft: Float? = null
    /**
     * 每个 item 右侧间距，单位 dp.
     */
    var itemRight: Float? = null
    /**
     * 每个 item 顶部间距，单位 dp.
     */
    var itemTop: Float? = null
    /**
     * 每个 item 底部间距，单位 dp.
     */
    var itemBottom: Float? = null


    /**
     * 屏幕缩放倍率，用于 dp 与 px 
     */
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

    /**
     * 当 RecyclerView 为纵向且只有 1 列时，设置间距.
     * @param itemCount 数据总条数
     */
    private fun setVerticalOne(outRect: Rect, position: Int, itemCount: Int) {
        val left: Int = dp2px(wholeLeft ?: ((itemLeft ?: 0f) * 2))
        val right: Int = dp2px(wholeRight ?: ((itemRight ?: 0f) * 2))
        val top: Int = dp2px(if (position == 0) wholeTop ?: ((itemTop ?: 0f) * 2) else (itemTop ?: 0f))
        val bottom: Int = dp2px(if (position == itemCount - 1) wholeBottom ?: ((itemBottom ?: 0f) * 2) else (itemBottom ?: 0f))
        outRect.set(left, top, right, bottom)
    }

    /**
     * 当 RecyclerView 为横向且只有 1 行时，设置间距.
     * @param itemCount 数据总条数
     */
    private fun setHorizontalOne(outRect: Rect, position: Int, itemCount: Int) {
        val left: Int = dp2px(if (position == 0) wholeLeft ?: ((itemLeft ?: 0f) * 2) else (itemLeft ?: 0f))
        val right: Int = dp2px(if (position == itemCount - 1) wholeRight ?: ((itemRight ?: 0f) * 2) else (itemRight ?: 0f))
        val top: Int = dp2px(wholeTop ?: ((itemTop ?: 0f) * 2))
        val bottom: Int = dp2px(wholeBottom ?: ((itemBottom ?: 0f) * 2))
        outRect.set(left, top, right, bottom)
    }

    /**
     * 当 RecyclerView 为纵向且有多列时，设置间距.
     * @param itemCount 数据总条数
     * @param spanCount 总列数(共有多少列)
     */
    private fun setVerticalMulti(outRect: Rect, position: Int, itemCount: Int, spanCount: Int) {
        val totalRow = itemCount / spanCount + if (itemCount % spanCount == 0) 0 else 1 //总行数
        val rowPosition = position / spanCount    //当前 position 在第几行[0, totalRow)
        val columnPosition = position % spanCount //当前 position 在第几列[0, spanCount)

        val left: Int = dp2px(if (columnPosition == 0) wholeLeft ?: ((itemLeft ?: 0f) * 2) else (itemLeft ?: 0f))
        val right: Int = dp2px(if (columnPosition == spanCount - 1) wholeRight ?: ((itemRight ?: 0f) * 2) else (itemRight ?: 0f))
        val top: Int = dp2px(if (rowPosition == 0) wholeTop ?: ((itemTop ?: 0f) * 2) else (itemTop ?: 0f))
        val bottom: Int = dp2px(if (rowPosition == totalRow - 1) wholeBottom ?: ((itemBottom ?: 0f) * 2) else (itemBottom ?: 0f))
        outRect.set(left, top, right, bottom)
    }

    /**
     * 当 RecyclerView 为纵向且为瀑布流布局时，设置间距.
     * @param itemCount 数据总条数
     * @param spanCount 总列数(共有多少列)
     * @param spanIndex 当前数据在列数中的index[0, spanCount)，即第几列
     */
    private fun setVerticalMultiStaggered(outRect: Rect, position: Int, itemCount: Int, spanCount: Int, spanIndex: Int) {
        val totalRow = itemCount / spanCount + if (itemCount % spanCount == 0) 0 else 1 //总行数
        val rowPosition = position / spanCount //当前position在第几行[0, totalRow)

        val left: Int = dp2px(if (spanIndex == 0) wholeLeft ?: ((itemLeft ?: 0f) * 2) else (itemLeft ?: 0f))
        val right: Int = dp2px(if (spanIndex == spanCount - 1) wholeRight ?: ((itemRight ?: 0f) * 2) else (itemRight ?: 0f))
        val top: Int = dp2px(if (rowPosition == 0) wholeTop ?: ((itemTop ?: 0f) * 2) else (itemTop ?: 0f))
        val bottom: Int = dp2px(if (rowPosition == totalRow - 1) wholeBottom ?: ((itemBottom ?: 0f) * 2) else (itemBottom ?: 0f))
        outRect.set(left, top, right, bottom)
    }

    /**
     * 当 RecyclerView 为横向且有多行时，设置间距.
     * @param itemCount 数据总条数
     * @param spanCount 总行数(共有多少行)
     */
    private fun setHorizontalMulti(outRect: Rect, position: Int, itemCount: Int, spanCount: Int) {
        // TODO: 有这样的需求了再来写吧
    }


    private fun dp2px(dpValue: Float): Int = (dpValue * density + 0.5f).toInt()
}