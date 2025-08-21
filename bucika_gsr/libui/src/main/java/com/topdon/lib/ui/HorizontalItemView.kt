package com.topdon.lib.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.ui.transform.HorizontalScrollItemTransformer
import com.topdon.lib.ui.utils.DSVOrientation
import com.topdon.lib.ui.utils.HorizontalLayoutManager

/**
 * @author: CaiSongL
 * @date: 2023/4/1 13:59
 */
@Deprecated("热成像-菜单-拍照已重构，不需要这个类了")
class HorizontalItemView : RecyclerView {
    private val layoutManager: HorizontalLayoutManager
    private val scrollStateChangeListeners: ArrayList<ScrollStateChangeListener<ViewHolder>> = ArrayList()
    private val onItemChangedListeners: ArrayList<OnItemChangedListener<ViewHolder>> = ArrayList()
    
    private var isOverScrollEnabled = false



    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        isOverScrollEnabled = overScrollMode != OVER_SCROLL_NEVER
        layoutManager = HorizontalLayoutManager(context, ScrollStateListener(),  DSVOrientation.HORIZONTAL)
        setLayoutManager(layoutManager)
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        if (layout is HorizontalLayoutManager) {
            super.setLayoutManager(layout)
        } else {
            throw IllegalArgumentException("You should not set LayoutManager on DiscreteScrollView")
        }
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val isFling = super.fling(velocityX, velocityY)
        if (isFling) {
            layoutManager.onFling(velocityX, velocityY)
        } else {
            layoutManager.returnToCurrentPosition()
        }
        return isFling
    }

    fun setItemTransitionTimeMillis(@IntRange(from = 10) millis: Int) {
        layoutManager.setTimeForItemSettle(millis)
    }

    fun setItemTransformer(transformer: HorizontalScrollItemTransformer?) {
        layoutManager.setItemTransformer(transformer)
    }

    fun setSlideOnFling(result: Boolean) {
        layoutManager.setShouldSlideOnFling(result)
    }

    fun addScrollStateChangeListener(scrollStateChangeListener: ScrollStateChangeListener<*>) {
        scrollStateChangeListeners.add(scrollStateChangeListener as ScrollStateChangeListener<ViewHolder>)
    }

    fun addOnItemChangedListener(onItemChangedListener: OnItemChangedListener<*>) {
        onItemChangedListeners.add(onItemChangedListener as OnItemChangedListener<ViewHolder>)
    }



    private inner class ScrollStateListener : HorizontalLayoutManager.ScrollStateListener {
        override fun onIsBoundReachedFlagChange(isBoundReached: Boolean) {
            if (isOverScrollEnabled) {
                overScrollMode = if (isBoundReached) OVER_SCROLL_ALWAYS else OVER_SCROLL_NEVER
            }
        }

        override fun onScrollStart() {
            if (scrollStateChangeListeners.isEmpty()) {
                return
            }
            val current: Int = layoutManager.currentPosition
            val holder: ViewHolder = getViewHolder(current) ?: return
            for (listener in scrollStateChangeListeners) {
                listener.onScrollStart(holder, current)
            }
        }

        override fun onScrollEnd() {
            if (onItemChangedListeners.isEmpty() && scrollStateChangeListeners.isEmpty()) {
                return
            }
            val current = layoutManager.currentPosition
            val holder = getViewHolder(current) ?: return
            for (listener in scrollStateChangeListeners) {
                listener.onScrollEnd(holder, current)
            }
            for (listener in onItemChangedListeners) {
                listener.onCurrentItemChanged(holder, current)
            }
        }

        override fun onScroll(currentViewPosition: Float) {
            if (scrollStateChangeListeners.isEmpty()) {
                return
            }
            val currentIndex: Int = layoutManager.currentPosition
            val newIndex = layoutManager.nextPosition
            if (currentIndex != newIndex) {
                val currentHolder: ViewHolder? = getViewHolder(currentIndex)
                val newHolder: ViewHolder? = getViewHolder(newIndex)
                for (listener in scrollStateChangeListeners) {
                    listener.onScroll(currentViewPosition, currentIndex, newIndex, currentHolder, newHolder)
                }
            }
        }

        override fun onCurrentViewFirstLayout() {
            post { notifyCurrentItemChanged() }
        }

        override fun onDataSetChangeChangedPosition() {
            notifyCurrentItemChanged()
        }

        private fun getViewHolder(position: Int): ViewHolder? {
            val view: View = layoutManager.findViewByPosition(position) ?: return null
            return getChildViewHolder(view)
        }

        private fun notifyCurrentItemChanged() {
            if (onItemChangedListeners.isEmpty()) {
                return
            }
            val current = layoutManager.currentPosition
            val currentHolder = getViewHolder(current)
            for (listener in onItemChangedListeners) {
                listener.onCurrentItemChanged(currentHolder, current)
            }
        }
    }

    interface ScrollStateChangeListener<T : ViewHolder> {
        fun onScrollStart(currentItemHolder: T, adapterPosition: Int)
        fun onScrollEnd(currentItemHolder: T, adapterPosition: Int)
        fun onScroll(scrollPosition: Float, currentPosition: Int, newPosition: Int, currentHolder: T?, newCurrent: T?)
    }

    interface OnItemChangedListener<T : ViewHolder?> {
        /*
         * This method will be also triggered when view appears on the screen for the first time.
         * If data set is empty, viewHolder will be null and adapterPosition will be NO_POSITION
         */
        fun onCurrentItemChanged(viewHolder: T?, adapterPosition: Int)
    }
}