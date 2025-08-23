package com.topdon.module.thermal.base

import android.graphics.Bitmap
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.topdon.lib.core.ktbase.BaseFragment
import kotlinx.coroutines.Job

open class BaseThermalFragment : BaseFragment() {
    var mIrBitmap: Bitmap? = null
    val REQUEST_CODE_FROM_UPGRADE = 1001
    val SRC_WIDTH = 192
    val SRC_HEIGHT = 256

    var paletteIndex = 0
    var irSurfaceViewLayoutParams: ConstraintLayout.LayoutParams? = null
    var displayViewLayoutParams: FrameLayout.LayoutParams? = null
    var fenceLayoutParams: FrameLayout.LayoutParams? = null
    var cameraLayoutParams: FrameLayout.LayoutParams? = null

    var mCenter = 0f
    var mMaxTemp = 0f
    var mMinTemp = 0f
    var maxImg: ImageView? = null
    var minImg: ImageView? = null
    var maxIndex = 0
    var minIndex = 0

    var mCenterTextView: TextView? = null
    var mMaxTextView: TextView? = null
    var mMinTextView: TextView? = null
    var mDistanceEditText: EditText? = null
    var mBrightEditText: EditText? = null
    var mContrastEditText: EditText? = null
    var mTempMatrixEditText: EditText? = null
    var mEmissEditText: EditText? = null
    var mUpgradePathEditText: EditText? = null
    var mFirmwareVersionTextView: TextView? = null
    var mSnTextView: TextView? = null
    var mIdTextView: TextView? = null
    var mDisplayFrameLayout: FrameLayout? = null
    var mFenceLayout: FrameLayout? = null
    var mCameraLayout: FrameLayout? = null
    var mExpertLayout: LinearLayout? = null

    var isDispLayTemp = false
    var timerJob: Job? = null
    val EXPERT_MODE_HIT_COUNT = 5
    val EXPERT_MODE_HIT_DURATION = (2 * 1000).toLong()
    var EXPERT_HITS = LongArray(EXPERT_MODE_HIT_COUNT)

    var rawWidth = 0
    var rawHeight = 0
    var highCrossWidth = 40
    var highCrossHeight = 40
    var rotateType = 0
    var irSurfaceViewWidth = 0
    var irSurfaceViewHeight = 0

    var width = 0
    var height = 0
    var mIsIrVideoStart = false

    override fun initContentView() = 0

    override fun initView() {

    }

    override fun initData() {

    }
}