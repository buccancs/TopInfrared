package com.topdon.lib.ui.transform

import android.view.View
import androidx.annotation.FloatRange
import com.topdon.lib.ui.core.Pivot
import com.topdon.lib.ui.core.Pivot.X
import com.topdon.lib.ui.core.Pivot.Y

/**
 * @author: CaiSongL
 * @date: 2023/4/1 16:02
 */
class ScaleTransformer : HorizontalScrollItemTransformer {
    private var pivotX: Pivot = X.CENTER.create()
    private var pivotY: Pivot = Y.CENTER.create()
    private var minScale: Float = 0.8f
    private var maxMinDiff: Float = 0.2f

    override fun transformItem(item: View, position: Float) {
        pivotX.setOn(item)
        pivotY.setOn(item)
        val closenessToCenter = 1f - Math.abs(position)
        val scale = minScale + maxMinDiff * closenessToCenter
        item.scaleX = scale
        item.scaleY = scale
    }

    class Builder {
        private val transformer: ScaleTransformer
        private var maxScale: Float

        init {
            transformer = ScaleTransformer()
            maxScale = 1f
        }

        fun setMinScale(@FloatRange(from = 0.01) scale: Float): Builder {
            transformer.minScale = scale
            return this
        }

        fun setMaxScale(@FloatRange(from = 0.01) scale: Float): Builder {
            maxScale = scale
            return this
        }

        fun setPivotX(pivotX: X): Builder {
            return setPivotX(pivotX.create())
        }

        fun setPivotX(pivot: Pivot): Builder {
            assertAxis(pivot, Pivot.AXIS_X)
            transformer.pivotX = pivot
            return this
        }

        fun setPivotY(pivotY: Y): Builder {
            return setPivotY(pivotY.create())
        }

        private fun setPivotY(pivot: Pivot): Builder {
            assertAxis(pivot, Pivot.AXIS_Y)
            transformer.pivotY = pivot
            return this
        }

        fun build(): ScaleTransformer {
            transformer.maxMinDiff = maxScale - transformer.minScale
            return transformer
        }

        private fun assertAxis(pivot: Pivot, @Pivot.Axis axis: Int) {
            require(pivot.axis == axis) { "You passed a Pivot for wrong axis." }
        }
    }
}