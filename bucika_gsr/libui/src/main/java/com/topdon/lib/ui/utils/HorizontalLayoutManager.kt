package com.topdon.lib.ui.utils

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityEventCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.ui.transform.HorizontalScrollItemTransformer
import com.topdon.lib.ui.utils.Direction.Companion.fromDelta
import java.util.*
import kotlin.math.abs

/**
 * @author: CaiSongL
 * @date: 2023/4/1 14:13
 */
@Deprecated("热成像-菜单-拍照已重构，不需要这个类了")
class HorizontalLayoutManager(
    private val context: Context,
    scrollStateListener: ScrollStateListener,
    orientation: DSVOrientation
) : RecyclerView.LayoutManager() {
    //This field will take value of all visible view's center points during the fill phase
    private var viewCenterIterator: Point
    private var recyclerCenter: Point
    private var currentViewCenter: Point
    private var childHalfWidth = 0
    private var childHalfHeight = 0
    var extraLayoutSpace = 0
        private set

    //Max possible distance a view can travel during one scroll phase
    private var scrollToChangeCurrent = 0
    private var currentScrollState = 0
    private var scrolled = 0
    private var pendingScroll = 0
    var currentPosition: Int
        private set
    private var pendingPosition: Int
    private var detachedCache: SparseArray<View>
    private var orientationHelper: DSVOrientation.Helper
    private var isFirstOrEmptyLayout = false
    private var timeForItemSettle: Int
    private var offscreenItems = 0
    private var transformClampItemCount: Int
    private var dataSetChangeShiftedPosition = false
    private var flingThreshold: Int
    private var shouldSlideOnFling: Boolean
    private var viewWidth = 0
    private var viewHeight = 0
    private val scrollStateListener: ScrollStateListener
    private var itemTransformer: HorizontalScrollItemTransformer? = null
    private var recyclerViewProxy: RecyclerViewProxy

    init {
        timeForItemSettle = DEFAULT_TIME_FOR_ITEM_SETTLE
        pendingPosition = NO_POSITION
        currentPosition = NO_POSITION
        flingThreshold = DEFAULT_FLING_THRESHOLD
        shouldSlideOnFling = false
        recyclerCenter = Point()
        currentViewCenter = Point()
        viewCenterIterator = Point()
        detachedCache = SparseArray()
        this.scrollStateListener = scrollStateListener
        orientationHelper = orientation.createHelper()
        recyclerViewProxy = RecyclerViewProxy(this)
        transformClampItemCount = DEFAULT_TRANSFORM_CLAMP_ITEM_COUNT
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            recyclerViewProxy.removeAndRecycleAllViews(recycler)
            pendingPosition = NO_POSITION
            currentPosition = pendingPosition
            pendingScroll = 0
            scrolled = pendingScroll
            return
        }
        ensureValidPosition(state)
        updateRecyclerDimensions(state)
        if (!isFirstOrEmptyLayout) {
            isFirstOrEmptyLayout = recyclerViewProxy.childCount == 0
            if (isFirstOrEmptyLayout) {
                initChildDimensions(recycler)
            }
        }
        recyclerViewProxy.detachAndScrapAttachedViews(recycler)
        fill(recycler)
        applyItemTransformToChildren()
    }

    private fun ensureValidPosition(state: RecyclerView.State) {
        if (currentPosition == NO_POSITION || currentPosition >= state.itemCount) {
            //currentPosition might have been assigned in onRestoreInstanceState()
            //which can lead to a crash (position out of bounds) when data set
            //is not persisted across rotations
            currentPosition = 0
        }
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        if (isFirstOrEmptyLayout) {
            scrollStateListener.onCurrentViewFirstLayout()
            isFirstOrEmptyLayout = false
        } else if (dataSetChangeShiftedPosition) {
            scrollStateListener.onDataSetChangeChangedPosition()
            dataSetChangeShiftedPosition = false
        }
    }

    private fun initChildDimensions(recycler: RecyclerView.Recycler?) {
        val viewToMeasure = recyclerViewProxy.getMeasuredChildForAdapterPosition(0, recycler!!)
        val childViewWidth = recyclerViewProxy.getMeasuredWidthWithMargin(viewToMeasure)
        val childViewHeight = recyclerViewProxy.getMeasuredHeightWithMargin(viewToMeasure)
        childHalfWidth = childViewWidth / 2
        childHalfHeight = childViewHeight / 2
        scrollToChangeCurrent = orientationHelper.getDistanceToChangeCurrent(
            childViewWidth,
            childViewHeight
        )
        extraLayoutSpace = scrollToChangeCurrent * offscreenItems
        recyclerViewProxy.detachAndScrapView(viewToMeasure, recycler)
    }

    private fun updateRecyclerDimensions(state: RecyclerView.State) {
        val dimensionsChanged = (!state.isMeasuring
                && (recyclerViewProxy.width != viewWidth
                || recyclerViewProxy.height != viewHeight))
        if (dimensionsChanged) {
            viewWidth = recyclerViewProxy.width
            viewHeight = recyclerViewProxy.height
            recyclerViewProxy.removeAllViews()
        }
        recyclerCenter[recyclerViewProxy.width / 2] = recyclerViewProxy.height / 2
    }

    private fun fill(recycler: RecyclerView.Recycler?) {
        cacheAndDetachAttachedViews()
        orientationHelper.setCurrentViewCenter(recyclerCenter, scrolled, currentViewCenter)
        val endBound = orientationHelper.getViewEnd(
            recyclerViewProxy.width,
            recyclerViewProxy.height
        )

        //Layout current
        if (isViewVisible(currentViewCenter, endBound)) {
            layoutView(recycler, currentPosition, currentViewCenter)
        }

        //Layout items before the current item
        layoutViews(recycler, Direction.START, endBound)

        //Layout items after the current item
        layoutViews(recycler, Direction.END, endBound)
        recycleDetachedViewsAndClearCache(recycler!!)
    }

    private fun layoutViews(recycler: RecyclerView.Recycler?, direction: Direction, endBound: Int) {
        val positionStep = direction.applyTo(1)

        //Predictive layout is required when we are doing smooth fast scroll towards pendingPosition
        var noPredictiveLayoutRequired = (pendingPosition == NO_POSITION
                || !direction.sameAs(pendingPosition - currentPosition))
        viewCenterIterator[currentViewCenter.x] = currentViewCenter.y
        var pos = currentPosition + positionStep
        while (isInBounds(pos)) {
            if (pos == pendingPosition) {
                noPredictiveLayoutRequired = true
            }
            orientationHelper.shiftViewCenter(direction, scrollToChangeCurrent, viewCenterIterator)
            if (isViewVisible(viewCenterIterator, endBound)) {
                layoutView(recycler, pos, viewCenterIterator)
            } else if (noPredictiveLayoutRequired) {
                break
            }
            pos += positionStep
        }
    }

    private fun layoutView(recycler: RecyclerView.Recycler?, position: Int, viewCenter: Point) {
        if (position < 0) return
        var v = detachedCache[position]
        if (v == null) {
            v = recyclerViewProxy.getMeasuredChildForAdapterPosition(position, recycler!!)
            recyclerViewProxy.layoutDecoratedWithMargins(
                v,
                viewCenter.x - childHalfWidth, viewCenter.y - childHalfHeight,
                viewCenter.x + childHalfWidth, viewCenter.y + childHalfHeight
            )
        } else {
            recyclerViewProxy.attachView(v)
            detachedCache.remove(position)
        }
    }

    private fun cacheAndDetachAttachedViews() {
        detachedCache.clear()
        for (i in 0 until recyclerViewProxy.childCount) {
            val child = recyclerViewProxy.getChildAt(i)
            detachedCache.put(recyclerViewProxy.getPosition(child), child)
        }
        for (i in 0 until detachedCache.size()) {
            recyclerViewProxy.detachView(detachedCache.valueAt(i))
        }
    }

    private fun recycleDetachedViewsAndClearCache(recycler: RecyclerView.Recycler) {
        for (i in 0 until detachedCache.size()) {
            val viewToRemove = detachedCache.valueAt(i)
            recyclerViewProxy.recycleView(viewToRemove, recycler)
        }
        detachedCache.clear()
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        var newPosition = currentPosition
        if (currentPosition == NO_POSITION) {
            newPosition = 0
        } else if (currentPosition >= positionStart) {
            newPosition = Math.min(currentPosition + itemCount, recyclerViewProxy.itemCount - 1)
        }
        onNewPosition(newPosition)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        var newPosition = currentPosition
        if (recyclerViewProxy.itemCount == 0) {
            newPosition = NO_POSITION
        } else if (currentPosition >= positionStart) {
            if (currentPosition < positionStart + itemCount) {
                //If currentPosition is in the removed items, then the new item became current
                currentPosition = NO_POSITION
            }
            newPosition = Math.max(0, currentPosition - itemCount)
        }
        onNewPosition(newPosition)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        //notifyDataSetChanged() was called. We need to ensure that currentPosition is not out of bounds
        currentPosition = Math.min(Math.max(0, currentPosition), recyclerViewProxy.itemCount - 1)
        dataSetChangeShiftedPosition = true
    }

    private fun onNewPosition(position: Int) {
        if (currentPosition != position) {
            currentPosition = position
            dataSetChangeShiftedPosition = true
        }
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return scrollBy(dx, recycler)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return scrollBy(dy, recycler)
    }

    private fun scrollBy(amount: Int, recycler: RecyclerView.Recycler?): Int {
        if (recyclerViewProxy.childCount == 0) {
            return 0
        }
        val direction = fromDelta(amount)
        val leftToScroll = calculateAllowedScrollIn(direction)
        if (leftToScroll <= 0) {
            return 0
        }
        val delta = direction.applyTo(Math.min(leftToScroll, Math.abs(amount)))
        scrolled += delta
        if (pendingScroll != 0) {
            pendingScroll -= delta
        }
        orientationHelper.offsetChildren(-delta, recyclerViewProxy)
        if (orientationHelper.hasNewBecomeVisible(this)) {
            fill(recycler)
        }
        notifyScroll()
        applyItemTransformToChildren()
        return delta
    }

    private fun applyItemTransformToChildren() {
        if (itemTransformer != null) {
            val clampAfterDistance = scrollToChangeCurrent * transformClampItemCount
            for (i in 0 until recyclerViewProxy.childCount) {
                val child = recyclerViewProxy.getChildAt(i)
                val position = getCenterRelativePositionOf(child!!, clampAfterDistance)
                itemTransformer!!.transformItem(child, position)
            }
        }
    }

    override fun scrollToPosition(position: Int) {
        if (currentPosition == position) {
            return
        }
        currentPosition = position
        recyclerViewProxy.requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (currentPosition == position || pendingPosition != NO_POSITION) {
            return
        }
        checkTargetPosition(state, position)
        if (currentPosition == NO_POSITION) {
            //Layout not happened yet
            currentPosition = position
        } else {
            startSmoothPendingScroll(position)
        }
    }

    override fun canScrollHorizontally(): Boolean {
        return orientationHelper.canScrollHorizontally()
    }

    override fun canScrollVertically(): Boolean {
        return orientationHelper.canScrollVertically()
    }

    override fun onScrollStateChanged(state: Int) {
        if (currentScrollState == RecyclerView.SCROLL_STATE_IDLE && currentScrollState != state) {
            scrollStateListener.onScrollStart()
        }
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            //Scroll is not finished until current view is centered
            val isScrollEnded = onScrollEnd()
            if (isScrollEnded) {
                scrollStateListener.onScrollEnd()
            } else {
                //Scroll continues and we don't want to set currentScrollState to STATE_IDLE,
                //because this will then trigger .scrollStateListener.onScrollStart()
                return
            }
        } else if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            onDragStart()
        }
        currentScrollState = state
    }

    /**
     * @return true if scroll is ended and we don't need to settle items
     */
    private fun onScrollEnd(): Boolean {
        if (pendingPosition != NO_POSITION) {
            currentPosition = pendingPosition
            pendingPosition = NO_POSITION
            scrolled = 0
        }
        val scrollDirection = fromDelta(scrolled)
        if (Math.abs(scrolled) == scrollToChangeCurrent) {
            currentPosition += scrollDirection.applyTo(1)
            scrolled = 0
        }
        pendingScroll = if (isAnotherItemCloserThanCurrent) {
            getHowMuchIsLeftToScroll(scrolled)
        } else {
            -scrolled
        }
        return if (pendingScroll == 0) {
            true
        } else {
            startSmoothPendingScroll()
            false
        }
    }

    private fun onDragStart() {
        //Here we need to:
        //1. Stop any pending scroll
        //2. Set currentPosition to position of the item that is closest to the center
        val isScrollingThroughMultiplePositions = Math.abs(scrolled) > scrollToChangeCurrent
        if (isScrollingThroughMultiplePositions) {
            val scrolledPositions = scrolled / scrollToChangeCurrent
            currentPosition += scrolledPositions
            scrolled -= scrolledPositions * scrollToChangeCurrent
        }
        if (isAnotherItemCloserThanCurrent) {
            val direction = fromDelta(scrolled)
            currentPosition += direction.applyTo(1)
            scrolled = -getHowMuchIsLeftToScroll(scrolled)
        }
        pendingPosition = NO_POSITION
        pendingScroll = 0
    }

    fun onFling(velocityX: Int, velocityY: Int) {
        val velocity = orientationHelper.getFlingVelocity(velocityX, velocityY)
        val throttleValue = if (shouldSlideOnFling) Math.abs(velocity / flingThreshold) else 1
        var newPosition = currentPosition + fromDelta(velocity).applyTo(throttleValue)
        newPosition = checkNewOnFlingPositionIsInBounds(newPosition)
        val isInScrollDirection = velocity * scrolled >= 0
        val canFling = isInScrollDirection && isInBounds(newPosition)
        if (canFling) {
            startSmoothPendingScroll(newPosition)
        } else {
            returnToCurrentPosition()
        }
    }

    fun returnToCurrentPosition() {
        pendingScroll = -scrolled
        if (pendingScroll != 0) {
            startSmoothPendingScroll()
        }
    }

    private fun calculateAllowedScrollIn(direction: Direction): Int {
        if (pendingScroll != 0) {
            return Math.abs(pendingScroll)
        }
        val allowedScroll: Int
        val isBoundReached: Boolean
        val isScrollDirectionAsBefore = direction.applyTo(scrolled) > 0
        if (direction === Direction.START && currentPosition == 0) {
            //We can scroll to the left when currentPosition == 0 only if we scrolled to the right before
            isBoundReached = scrolled == 0
            allowedScroll = if (isBoundReached) 0 else Math.abs(scrolled)
        } else if (direction === Direction.END && currentPosition == recyclerViewProxy.itemCount - 1) {
            //We can scroll to the right when currentPosition == last only if we scrolled to the left before
            isBoundReached = scrolled == 0
            allowedScroll = if (isBoundReached) 0 else Math.abs(scrolled)
        } else {
            isBoundReached = false
            allowedScroll =
                if (isScrollDirectionAsBefore) scrollToChangeCurrent - Math.abs(scrolled) else scrollToChangeCurrent + Math.abs(
                    scrolled
                )
        }
        scrollStateListener.onIsBoundReachedFlagChange(isBoundReached)
        return allowedScroll
    }

    private fun startSmoothPendingScroll() {
        val scroller: LinearSmoothScroller = DiscreteLinearSmoothScroller(context)
        scroller.targetPosition = currentPosition
        recyclerViewProxy.startSmoothScroll(scroller)
    }

    private fun startSmoothPendingScroll(position: Int) {
        if (currentPosition == position) return
        pendingScroll = -scrolled
        val direction = fromDelta(position - currentPosition)
        val distanceToScroll = Math.abs(position - currentPosition) * scrollToChangeCurrent
        pendingScroll += direction.applyTo(distanceToScroll)
        pendingPosition = position
        startSmoothPendingScroll()
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        return computeScrollRange(state)
    }

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset(state)
    }

    override fun computeVerticalScrollExtent(state: RecyclerView.State): Int {
        return computeScrollExtent(state)
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return computeScrollRange(state)
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset(state)
    }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return computeScrollExtent(state)
    }

    private fun computeScrollOffset(state: RecyclerView.State): Int {
        val scrollbarSize = computeScrollExtent(state)
        val offset = (scrolled / scrollToChangeCurrent.toFloat() * scrollbarSize).toInt()
        return currentPosition * scrollbarSize + offset
    }

    private fun computeScrollExtent(state: RecyclerView.State): Int {
        return if (itemCount == 0) {
            0
        } else {
            (computeScrollRange(state) / itemCount.toFloat()).toInt()
        }
    }

    private fun computeScrollRange(state: RecyclerView.State): Int {
        return if (itemCount == 0) {
            0
        } else {
            scrollToChangeCurrent * (itemCount - 1)
        }
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        pendingPosition = NO_POSITION
        pendingScroll = 0
        scrolled = pendingScroll
        currentPosition = if (newAdapter is InitialPositionProvider) {
            (newAdapter as InitialPositionProvider).getInitialPosition()
        } else {
            0
        }
        recyclerViewProxy.removeAllViews()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        if (pendingPosition != NO_POSITION) {
            currentPosition = pendingPosition
        }
        bundle.putInt(EXTRA_POSITION, currentPosition)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        currentPosition = bundle.getInt(EXTRA_POSITION)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val nextPosition: Int
        get() = if (scrolled == 0) {
            currentPosition
        } else if (pendingPosition != NO_POSITION) {
            pendingPosition
        } else {
            currentPosition + fromDelta(scrolled).applyTo(1)
        }

    fun setItemTransformer(itemTransformer: HorizontalScrollItemTransformer?) {
        this.itemTransformer = itemTransformer
    }

    fun setTimeForItemSettle(timeForItemSettle: Int) {
        this.timeForItemSettle = timeForItemSettle
    }

    fun setOffscreenItems(offscreenItems: Int) {
        this.offscreenItems = offscreenItems
        extraLayoutSpace = scrollToChangeCurrent * offscreenItems
        recyclerViewProxy.requestLayout()
    }

    fun setTransformClampItemCount(transformClampItemCount: Int) {
        this.transformClampItemCount = transformClampItemCount
        applyItemTransformToChildren()
    }

    fun setOrientation(orientation: DSVOrientation) {
        orientationHelper = orientation.createHelper()
        recyclerViewProxy.removeAllViews()
        recyclerViewProxy.requestLayout()
    }

    fun setShouldSlideOnFling(result: Boolean) {
        shouldSlideOnFling = result
    }

    fun setSlideOnFlingThreshold(threshold: Int) {
        flingThreshold = threshold
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        if (recyclerViewProxy.childCount > 0) {
            val record = AccessibilityEventCompat.asRecord(event)
            record.fromIndex = getPosition(firstChild!!)
            record.toIndex = getPosition(lastChild!!)
        }
    }

    private fun getCenterRelativePositionOf(v: View, maxDistance: Int): Float {
        val distanceFromCenter = orientationHelper.getDistanceFromCenter(
            recyclerCenter,
            getDecoratedLeft(v) + childHalfWidth,
            getDecoratedTop(v) + childHalfHeight
        )
        return Math.min(Math.max(-1f, distanceFromCenter / maxDistance), 1f)
    }

    private fun checkNewOnFlingPositionIsInBounds(position: Int): Int {
        val itemCount = recyclerViewProxy.itemCount
        //The check is required in case slide through multiple items is turned on
        if (currentPosition != 0 && position < 0) {
            //If currentPosition == 0 && position < 0 we forbid scroll to the left,
            //but if currentPosition != 0 we can slide to the first item
            return 0
        } else if (currentPosition != itemCount - 1 && position >= itemCount) {
            return itemCount - 1
        }
        return position
    }

    private fun getHowMuchIsLeftToScroll(dx: Int): Int {
        return fromDelta(dx).applyTo(scrollToChangeCurrent - Math.abs(scrolled))
    }

    private val isAnotherItemCloserThanCurrent: Boolean
        private get() = abs(scrolled) >= scrollToChangeCurrent * SCROLL_TO_SNAP_TO_ANOTHER_ITEM
    val firstChild: View?
        get() = recyclerViewProxy.getChildAt(0)
    val lastChild: View?
        get() = recyclerViewProxy.getChildAt(recyclerViewProxy.childCount - 1)

    private fun notifyScroll() {
        val amountToScroll = if (pendingPosition != NO_POSITION) Math.abs(scrolled + pendingScroll)
            .toFloat() else scrollToChangeCurrent.toFloat()
        val position = -Math.min(Math.max(-1f, scrolled / amountToScroll), 1f)
        scrollStateListener.onScroll(position)
    }

    private fun isInBounds(itemPosition: Int): Boolean {
        return itemPosition >= 0 && itemPosition < recyclerViewProxy.itemCount
    }

    private fun isViewVisible(viewCenter: Point, endBound: Int): Boolean {
        return orientationHelper.isViewVisible(
            viewCenter, childHalfWidth, childHalfHeight,
            endBound, extraLayoutSpace
        )
    }

    private fun checkTargetPosition(state: RecyclerView.State, targetPosition: Int) {
        require(!(targetPosition < 0 || targetPosition >= state.itemCount)) {
            String.format(
                Locale.US,
                "target position out of bounds: position=%d, itemCount=%d",
                targetPosition, state.itemCount
            )
        }
    }

    private fun setRecyclerViewProxy(recyclerViewProxy: RecyclerViewProxy) {
        this.recyclerViewProxy = recyclerViewProxy
    }

    private fun setOrientationHelper(orientationHelper: DSVOrientation.Helper) {
        this.orientationHelper = orientationHelper
    }

    private inner class DiscreteLinearSmoothScroller(context: Context?) :
        LinearSmoothScroller(context) {
        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            return orientationHelper.getPendingDx(-pendingScroll)
        }

        override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
            return orientationHelper.getPendingDy(-pendingScroll)
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            val dist = Math.min(Math.abs(dx), scrollToChangeCurrent).toFloat()
            return (Math.max(0.01f, dist / scrollToChangeCurrent) * timeForItemSettle).toInt()
        }

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return PointF(
                orientationHelper.getPendingDx(pendingScroll).toFloat(),
                orientationHelper.getPendingDy(pendingScroll).toFloat()
            )
        }
    }

    interface ScrollStateListener {
        fun onIsBoundReachedFlagChange(isBoundReached: Boolean)
        fun onScrollStart()
        fun onScrollEnd()
        fun onScroll(currentViewPosition: Float)
        fun onCurrentViewFirstLayout()
        fun onDataSetChangeChangedPosition()
    }

    interface InitialPositionProvider {
        fun getInitialPosition() : Int
    }

    companion object {
        const val NO_POSITION = -1
        private const val EXTRA_POSITION = "extra_position"
        private const val DEFAULT_TIME_FOR_ITEM_SETTLE = 300
        private const val DEFAULT_FLING_THRESHOLD = 2100 //Decrease to increase sensitivity.
        private const val DEFAULT_TRANSFORM_CLAMP_ITEM_COUNT = 1
        private const val SCROLL_TO_SNAP_TO_ANOTHER_ITEM = 0.6f
    }
}