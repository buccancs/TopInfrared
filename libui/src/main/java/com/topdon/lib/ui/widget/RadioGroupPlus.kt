package com.topdon.lib.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.IdRes

class RadioGroupPlus : LinearLayout {
    
    @get:IdRes
    var checkedRadioButtonId = -1
        private set

    private var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    private var mProtectFromCheckedChange = false
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private var mPassThroughListener: PassThroughHierarchyChangeListener? = null

    constructor(context: Context?) : super(context) {
        orientation = VERTICAL
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        init()
    }

    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        mPassThroughListener = PassThroughHierarchyChangeListener()
        super.setOnHierarchyChangeListener(mPassThroughListener)
    }

    override fun setOnHierarchyChangeListener(listener: OnHierarchyChangeListener) {
        mPassThroughListener!!.mOnHierarchyChangeListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (checkedRadioButtonId != -1) {
            mProtectFromCheckedChange = true
            setCheckedStateForView(checkedRadioButtonId, true)
            mProtectFromCheckedChange = false
            setCheckedId(checkedRadioButtonId)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is RadioButton) {
            val button = child
            if (button.isChecked) {
                mProtectFromCheckedChange = true
                if (checkedRadioButtonId != -1) {
                    setCheckedStateForView(checkedRadioButtonId, false)
                }
                mProtectFromCheckedChange = false
                setCheckedId(button.id)
            }
        }
        super.addView(child, index, params)
    }

    fun check(@IdRes id: Int) {
        if (id != -1 && id == checkedRadioButtonId) {
            return
        }
        if (checkedRadioButtonId != -1) {
            setCheckedStateForView(checkedRadioButtonId, false)
        }
        if (id != -1) {
            setCheckedStateForView(id, true)
        }
        setCheckedId(id)
    }

    private fun setCheckedId(@IdRes id: Int) {
        checkedRadioButtonId = id
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener!!.onCheckedChanged(this, checkedRadioButtonId)
        }
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView = findViewById<View>(viewId)
        if (checkedView != null && checkedView is RadioButton) {
            checkedView.isChecked = checked
        }
    }

    fun clearCheck() {
        check(-1)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        mOnCheckedChangeListener = listener
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is RadioGroup.LayoutParams
    }

    override fun generateDefaultLayoutParams(): LinearLayout.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun getAccessibilityClassName(): CharSequence {
        return RadioGroup::class.java.name
    }

    class LayoutParams : LinearLayout.LayoutParams {
        
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {}

        constructor(w: Int, h: Int) : super(w, h) {}

        constructor(w: Int, h: Int, initWeight: Float) : super(w, h, initWeight) {}

        constructor(p: ViewGroup.LayoutParams?) : super(p) {}

        constructor(source: MarginLayoutParams?) : super(source) {}

        override fun setBaseAttributes(
            a: TypedArray,
            widthAttr: Int, heightAttr: Int
        ) {
            width = if (a.hasValue(widthAttr)) {
                a.getLayoutDimension(widthAttr, "layout_width")
            } else {
                WRAP_CONTENT
            }
            height = if (a.hasValue(heightAttr)) {
                a.getLayoutDimension(heightAttr, "layout_height")
            } else {
                WRAP_CONTENT
            }
        }
    }

    interface OnCheckedChangeListener {
        
        fun onCheckedChanged(group: RadioGroupPlus, @IdRes checkedId: Int)
    }

    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (mProtectFromCheckedChange) {
                return
            }
            mProtectFromCheckedChange = true
            if (checkedRadioButtonId != -1) {
                setCheckedStateForView(checkedRadioButtonId, false)
            }
            mProtectFromCheckedChange = false
            val id = buttonView.id
            setCheckedId(id)
        }
    }

    private inner class PassThroughHierarchyChangeListener :
        OnHierarchyChangeListener {
        var mOnHierarchyChangeListener: OnHierarchyChangeListener? = null
        fun traverseTree(view: View) {
            if (view is RadioButton) {
                var id = view.getId()
                if (id == NO_ID) {
                    id = generateViewId()
                    view.setId(id)
                }
                view.setOnCheckedChangeListener(
                    mChildOnCheckedChangeListener
                )
            }
            if (view !is ViewGroup) {
                return
            }
            val viewGroup = view
            if (viewGroup.childCount == 0) {
                return
            }
            for (i in 0 until viewGroup.childCount) {
                traverseTree(viewGroup.getChildAt(i))
            }
        }

        override fun onChildViewAdded(parent: View, child: View) {
            traverseTree(child)
            if (parent === this@RadioGroupPlus && child is RadioButton) {
                var id = child.getId()
                if (id == NO_ID) {
                    id = generateViewId()
                    child.setId(id)
                }
                child.setOnCheckedChangeListener(
                    mChildOnCheckedChangeListener
                )
            }
            mOnHierarchyChangeListener?.onChildViewAdded(parent, child)
        }

        override fun onChildViewRemoved(parent: View, child: View) {
            if (parent === this@RadioGroupPlus && child is RadioButton) {
                child.setOnCheckedChangeListener(null)
            }
            mOnHierarchyChangeListener?.onChildViewRemoved(parent, child)
        }
    }
}