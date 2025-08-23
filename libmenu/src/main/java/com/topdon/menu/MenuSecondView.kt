package com.topdon.menu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.repository.GalleryRepository
import com.topdon.menu.constant.MenuType
import com.topdon.menu.adapter.ColorAdapter
import com.topdon.menu.adapter.FenceAdapter
import com.topdon.menu.adapter.SettingAdapter
import com.topdon.menu.adapter.TargetAdapter
import com.topdon.menu.adapter.TempLevelAdapter
import com.topdon.menu.adapter.TempPointAdapter
import com.topdon.menu.adapter.TempSourceAdapter
import com.topdon.menu.adapter.TwoLightAdapter
import com.topdon.menu.constant.FenceType
import com.topdon.menu.constant.SettingType
import com.topdon.menu.constant.TargetType
import com.topdon.menu.constant.TempPointType
import com.topdon.menu.constant.TwoLightType
import com.topdon.menu.databinding.ViewMenuSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("NotifyDataSetChanged")
class MenuSecondView : FrameLayout {
    
    private val menuType: MenuType

    private lateinit var binding: ViewMenuSecondBinding

    fun selectPosition(position: Int) {
        binding.cameraMenuView.isVisible = position == 0 || position == 10
        binding.recyclerFence.isVisible = position == 1
        binding.recyclerTwoLight.isVisible = position == 2
        binding.recyclerColor.isVisible = position == 3 || position == 12
        binding.recyclerSettingTe.isVisible = position == 4
        binding.recyclerTempLevel.isVisible = position == 5

        binding.recyclerTempSource.isVisible = position == 11
        binding.recyclerTarget.isVisible = position == 13
        binding.recyclerTempPoint.isVisible = position == 14
        binding.recyclerSettingOb.isVisible = position == 15
    }

    var onCameraClickListener: ((actionCode: Int) -> Unit)?
        get() = binding.cameraMenuView.onCameraClickListener
        set(value) {
            binding.cameraMenuView.onCameraClickListener = value
        }
    
    var onFenceListener: ((fenceType: FenceType, isSelected: Boolean) -> Unit)?
        get() = fenceAdapter.onFenceListener
        set(value) {
            fenceAdapter.onFenceListener = value
        }
    
    var onTwoLightListener: ((twoLightType: TwoLightType, isSelected: Boolean) -> Unit)?
        get() = twoLightAdapter.onTwoLightListener
        set(value) {
            twoLightAdapter.onTwoLightListener = value
        }
    
    var onColorListener: ((index: Int, code: Int, size: Int) -> Unit)?
        get() = colorAdapter.onColorListener
        set(value) {
            colorAdapter.onColorListener = value
        }
    
    var onSettingListener: ((type: SettingType, isSelected: Boolean) -> Unit)?
        get() = settingTeAdapter.onSettingListener
        set(value) {
            settingTeAdapter.onSettingListener = value
            settingObAdapter.onSettingListener = value
        }
    
    var onTempLevelListener: ((code: Int) -> Unit)?
        get() = tempLevelAdapter.onTempLevelListener
        set(value) {
            tempLevelAdapter.onTempLevelListener = value
        }

    var onTempSourceListener: ((code: Int) -> Unit)?
        get() = tempSourceAdapter.onTempSourceListener
        set(value) {
            tempSourceAdapter.onTempSourceListener = value
        }
    
    var onTargetListener: ((targetType: TargetType) -> Unit)?
        get() = targetAdapter.onTargetListener
        set(value) {
            targetAdapter.onTargetListener = value
        }
    
    var onTempPointListener: ((type: TempPointType, isSelected: Boolean) -> Unit)?
        get() = tempPointAdapter.onTempPointListener
        set(value) {
            tempPointAdapter.onTempPointListener = value
        }

    private val fenceAdapter: FenceAdapter
    
    private val twoLightAdapter: TwoLightAdapter
    
    private val colorAdapter = ColorAdapter()
    
    private val settingTeAdapter: SettingAdapter
    
    private val tempLevelAdapter: TempLevelAdapter

    private val tempSourceAdapter = TempSourceAdapter()
    
    private val targetAdapter = TargetAdapter()
    
    private val tempPointAdapter = TempPointAdapter()
    
    private val settingObAdapter = SettingAdapter(isObserver = true)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuSecondView, defStyleAttr, defStyleRes)
        menuType = when (typedArray.getInt(R.styleable.MenuSecondView_deviceType, 0)) {
            0 -> MenuType.SINGLE_LIGHT
            1 -> MenuType.DOUBLE_LIGHT
            2 -> MenuType.Lite
            4 -> MenuType.GALLERY_EDIT
            else -> MenuType.TC007
        }
        typedArray.recycle()

        if (isInEditMode) {
            LayoutInflater.from(context).inflate(R.layout.view_menu_second, this, true)
            fenceAdapter = FenceAdapter(menuType)
            twoLightAdapter = TwoLightAdapter(menuType)
            settingTeAdapter = SettingAdapter(menuType)
            tempLevelAdapter = TempLevelAdapter(menuType)
        } else {
            binding = ViewMenuSecondBinding.inflate(LayoutInflater.from(context), this, true)

            refreshImg(GalleryRepository.DirType.LINE)

            fenceAdapter = FenceAdapter(menuType)
            binding.recyclerFence.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerFence.adapter = fenceAdapter

            twoLightAdapter = TwoLightAdapter(menuType)
            binding.recyclerTwoLight.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTwoLight.adapter = twoLightAdapter

            binding.recyclerColor.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerColor.adapter = colorAdapter

            settingTeAdapter = SettingAdapter(menuType)
            binding.recyclerSettingTe.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerSettingTe.adapter = settingTeAdapter

            tempLevelAdapter = TempLevelAdapter(menuType)
            binding.recyclerTempLevel.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTempLevel.adapter = tempLevelAdapter

            binding.recyclerTempSource.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTempSource.adapter = tempSourceAdapter

            binding.recyclerTarget.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTarget.adapter = targetAdapter

            binding.recyclerTempPoint.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTempPoint.adapter = tempPointAdapter

            binding.recyclerSettingOb.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerSettingOb.adapter = settingObAdapter
        }
    }

    var isVideoMode: Boolean
        get() = binding.cameraMenuView.isVideoMode
        set(value) {
            binding.cameraMenuView.isVideoMode = value
        }

    fun switchToCamera() {
        binding.cameraMenuView.canSwitchMode = true
        binding.cameraMenuView.isVideoMode = false
        binding.cameraMenuView.setToNormal()
    }

    fun updateCameraModel() {
        binding.cameraMenuView.canSwitchMode = true
        binding.cameraMenuView.setToNormal()
    }

    fun refreshImg(type: GalleryRepository.DirType = GalleryRepository.DirType.LINE) {
        updateCameraModel()
        CoroutineScope(Dispatchers.IO).launch {
            val path = GalleryRepository.readLatest(type)
            launch(Dispatchers.Main) {
                binding.cameraMenuView.refreshGallery(path)
            }
        }
    }

    fun setToRecord(isDelay: Boolean) {
        binding.cameraMenuView.canSwitchMode = false
        binding.cameraMenuView.setToRecord(isDelay)
    }

    fun setToCamera() {
        binding.cameraMenuView.setToRecord(false)
    }

    var fenceSelectType: FenceType?
        get() = fenceAdapter.selectType
        set(value) {
            fenceAdapter.selectType = value
        }

    var twoLightType: TwoLightType
        get() = twoLightAdapter.twoLightType
        set(value) {
            twoLightAdapter.twoLightType = value
        }

    fun setTwoLightSelected(twoLightType: TwoLightType, isSelected: Boolean) {
        twoLightAdapter.setSelected(twoLightType, isSelected)
    }

    fun setPseudoColor(code: Int) {
        colorAdapter.selectCode = code
    }

    fun setSettingSelected(settingType: SettingType, isSelected: Boolean) {
        settingTeAdapter.setSelected(settingType, isSelected)
        settingObAdapter.setSelected(settingType, isSelected)
    }

    fun setSettingRotate(rotateAngle: Int) {
        settingTeAdapter.rotateAngle = rotateAngle
        settingObAdapter.rotateAngle = rotateAngle
    }

    var isUnitF: Boolean
        get() = tempLevelAdapter.isUnitF
        set(value) {
            tempLevelAdapter.isUnitF = value
        }
    
    fun setTempLevel(code: Int) {
        tempLevelAdapter.selectCode = code
    }

    fun setTempSource(code: Int) {
        tempSourceAdapter.selectCode = code
    }

    fun setTargetSelected(targetType: TargetType, isSelected: Boolean) {
        targetAdapter.setSelected(targetType, isSelected)
    }
    
    fun setTargetMode(modeCode: Int) {
        targetAdapter.setTargetMode(modeCode)
    }

    fun setTempPointSelect(tempPointType: TempPointType, isSelected: Boolean) {
        tempPointAdapter.setSelected(tempPointType, isSelected)
    }
    
    fun clearTempPointSelect() {
        tempPointAdapter.clearAllSelect()
    }
}