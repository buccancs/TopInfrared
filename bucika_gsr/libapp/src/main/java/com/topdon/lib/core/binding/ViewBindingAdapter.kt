package com.topdon.lib.core.binding

import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.SizeUtils

/**
 * RecyclerView 的 BindingAdapter.
 *
 * Created by LCG on 2024/11/5.
 */
object ViewBindingAdapter {

    /**
     * 为 view 的 background 添加或移除 selectableItemBackground 效果.
     */
    @JvmStatic
    @BindingAdapter("bgEffect")
    fun setBgEffect(view: View, wantEffect: Boolean) {
        val oldDrawable: Drawable? = view.background
        if (oldDrawable is LayerDrawable) {
            val layerCount = oldDrawable.numberOfLayers
            val drawableList = ArrayList<Drawable>(layerCount + 1)
            for (i in 0 until layerCount) {
                if (oldDrawable.getId(i) == android.R.id.hint) {
                    if (wantEffect) {
                        return
                    }
                } else {
                    drawableList.add(oldDrawable.getDrawable(i))
                }
            }
            if (wantEffect) {
                val typedArray: TypedArray = view.context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
                val effectDrawable: Drawable? = typedArray.getDrawable(0)
                typedArray.recycle()
                if (effectDrawable != null) {
                    drawableList.add(effectDrawable)
                }
            } else {
                if (drawableList.size == layerCount) { //本来就没有 hint
                    return
                }
                if (drawableList.isEmpty()) { //只有1个且为 hint，移除
                    view.background = null
                    return
                }
            }

            if (drawableList.size == 1) {
                view.background = drawableList[0]
                return
            }

            val newDrawable = LayerDrawable(drawableList.toArray(arrayOf()))
            if (drawableList.size == 2 && drawableList[0] is GradientDrawable) {
                oldDrawable.setId(0, android.R.id.content)
            }
            if (wantEffect) {
                oldDrawable.setId(drawableList.size - 1, android.R.id.hint)
            }
            view.background = newDrawable
        } else {
            val typedArray: TypedArray = view.context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            val effectDrawable: Drawable? = typedArray.getDrawable(0)
            typedArray.recycle()

            val newDrawable = LayerDrawable(if (oldDrawable == null) arrayOf(effectDrawable) else arrayOf(oldDrawable, effectDrawable))
            if (oldDrawable is GradientDrawable) {
                newDrawable.setId(0, android.R.id.content)
            }
            newDrawable.setId(if (oldDrawable == null) 0 else 1, android.R.id.hint)
            view.background = newDrawable
        }
    }

    /**
     * 使用矩形 shape 将指定 view 的 background 填充颜色设置为指定颜色值.
     *
     * 注意：最好搭配其他 bgXXX 一起设置，只需要设置颜色的话用原生的 android:background 不是更好？
     */
    @JvmStatic
    @BindingAdapter("bgColor")
    fun setBgColor(view: View, @ColorInt color: Int) {
        val gradientDrawable: GradientDrawable = buildGradientDrawable(view)
        gradientDrawable.setColor(color)
        view.background = buildEffectDrawable(view, gradientDrawable)
    }

    /**
     * 使用矩形 shape 为指定 view 的 background 设置圆角，单位**dp**.
     *
     * 注意：最好搭配其他 bgXXX 一起设置，否则光有圆角没颜色就相当于没设置。
     * @param bgCorners 4个角的圆角值，单位dp
     * @param bgCornersLT left-top 的圆角值，优先使用该值，单位dp
     * @param bgCornersRT right-top 的圆角值，优先使用该值，单位dp
     * @param bgCornersLB left-bottom 的圆角值，优先使用该值，单位dp
     * @param bgCornersRB right-bottom 的圆角值，优先使用该值，单位dp
     */
    @JvmStatic
    @BindingAdapter(value = ["bgCorners", "bgCornersLT", "bgCornersRT", "bgCornersLB", "bgCornersRB"], requireAll = false)
    fun setBgCorners(view: View, bgCorners: Int = 0, bgCornersLT: Int?, bgCornersRT: Int?, bgCornersLB: Int?, bgCornersRB: Int?) {
        val lt: Int = SizeUtils.dp2px(bgCornersLT?.toFloat() ?: bgCorners.toFloat())
        val rt: Int = SizeUtils.dp2px(bgCornersRT?.toFloat() ?: bgCorners.toFloat())
        val lb: Int = SizeUtils.dp2px(bgCornersLB?.toFloat() ?: bgCorners.toFloat())
        val rb: Int = SizeUtils.dp2px(bgCornersRB?.toFloat() ?: bgCorners.toFloat())
        val radii = floatArrayOf(lt.toFloat(), lt.toFloat(), rt.toFloat(), rt.toFloat(), rb.toFloat(), rb.toFloat(), lb.toFloat(), lb.toFloat())
        val gradientDrawable: GradientDrawable = buildGradientDrawable(view)
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadii = radii
        view.background = buildEffectDrawable(view, gradientDrawable)
    }

    /**
     * 使用矩形 shape 为指定 view 的 background 设置描边.
     * @param width 描边宽度，单位dp
     * @param color 描边颜色值
     */
    @JvmStatic
    @BindingAdapter(value = ["bgStrokeWidth", "bgStrokeColor"], requireAll = false)
    fun setBgStroke(view: View, width: Int, @ColorInt color: Int) {
        val gradientDrawable: GradientDrawable = buildGradientDrawable(view)
        gradientDrawable.setStroke(SizeUtils.dp2px(width.toFloat()), color)
        view.background = buildEffectDrawable(view, gradientDrawable)
    }

    /**
     * 使用矩形 shape 为指定 view 的 background 设置渐变颜色值.
     */
    @JvmStatic
    @BindingAdapter(value = ["bgStartColor", "bgCenterColor", "bgEndColor"], requireAll = false)
    fun setBgGradientColor(view: View, @ColorInt startColor: Int, @ColorInt centerColor: Int?, @ColorInt endColor: Int) {
        val gradientDrawable: GradientDrawable = buildGradientDrawable(view)
        gradientDrawable.colors = if (centerColor == null) intArrayOf(startColor, endColor) else intArrayOf(startColor, centerColor, endColor)
        view.background = buildEffectDrawable(view, gradientDrawable)
    }

    /**
     * 使用矩形 shape 为指定 view 的 background 设置指定类型渐变参数.
     * @param angle 线性渐变：渐变角度，必须为 45 的倍数，0为从左到右 90为从上到下 -90或270为从下到上
     * @param radius 放射渐变：直径百分比
     * @param centerX 放射渐变或扫描渐变：中心点X轴百分比
     * @param centerY 放射渐变或扫描渐变：中心点Y轴百分比
     */
    @JvmStatic
    @BindingAdapter(value = ["bgAngle", "bgRadius", "bgCenterX", "bgCenterY"], requireAll = false)
    fun setBgGradient(view: View, angle: Int?, radius: Float?, centerX: Float?, centerY: Float?) {
        val gradientDrawable: GradientDrawable = buildGradientDrawable(view)
        if (angle == null) {
            if (radius == null) {
                gradientDrawable.gradientType = GradientDrawable.SWEEP_GRADIENT
            } else {
                gradientDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientDrawable.gradientRadius = radius
            }
            if (centerX != null && centerY != null) {
                gradientDrawable.setGradientCenter(centerX, centerY)
            }
        } else {
            gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            gradientDrawable.orientation = getOrientation(angle)
        }
        view.background = buildEffectDrawable(view, gradientDrawable)
    }

    @JvmStatic
    private fun getOrientation(angle: Int): GradientDrawable.Orientation {
        return when ((angle % 360 + 360) % 360) {
            0 -> GradientDrawable.Orientation.LEFT_RIGHT
            45 -> GradientDrawable.Orientation.BL_TR
            90 -> GradientDrawable.Orientation.BOTTOM_TOP
            135 -> GradientDrawable.Orientation.BR_TL
            180 -> GradientDrawable.Orientation.RIGHT_LEFT
            225 -> GradientDrawable.Orientation.TR_BL
            270 -> GradientDrawable.Orientation.TOP_BOTTOM
            315 -> GradientDrawable.Orientation.TL_BR
            else -> GradientDrawable.Orientation.LEFT_RIGHT
        }
    }

    /**
     * 从指定 view 的 background 中获取 GradientDrawable.
     *
     * 若指定 view 的 background 为 GradientDrawable，则直接返回；
     *
     * 若指定 view 的 background 为 ColorDrawable，返回新的相应颜色的 GradientDrawable；
     *
     * 若指定 view 的 background 为 LayerDrawable，则查找 background id 的 GradientDrawable；
     *
     * 其他情况新建 GradientDrawable 并返回。
     */
    @JvmStatic
    private fun buildGradientDrawable(view: View): GradientDrawable {
        val oldDrawable: Drawable? = view.background
        if (oldDrawable is GradientDrawable) {
            return oldDrawable
        }
        if (oldDrawable is ColorDrawable) {
            val drawable = GradientDrawable()
            drawable.setColor(oldDrawable.color)
            return drawable
        }
        if (oldDrawable is LayerDrawable) {
            val drawable: Drawable? = oldDrawable.findDrawableByLayerId(android.R.id.content)
            if (drawable is GradientDrawable) {
                return drawable
            }
        }
        return GradientDrawable()
    }

    /**
     * 如果 view 此前的 background 已设置 bgEffect，则使用 bgDrawable 构建包含 bgEffect 的 LayerDrawable；
     * 否则直接返回 bgDrawable
     */
    @JvmStatic
    private fun buildEffectDrawable(view: View, bgDrawable: GradientDrawable): Drawable {
        val oldDrawable: Drawable? = view.background
        if (oldDrawable is LayerDrawable) {
            val effectDrawable: Drawable = oldDrawable.findDrawableByLayerId(android.R.id.hint) ?: return bgDrawable
            val newDrawable = LayerDrawable(arrayOf(bgDrawable, effectDrawable))
            newDrawable.setId(0, android.R.id.content)
            newDrawable.setId(1, android.R.id.hint)
            return newDrawable
        }
        return bgDrawable
    }
}