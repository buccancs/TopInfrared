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

/**
 * 二级菜单
 */
@SuppressLint("NotifyDataSetChanged")
class MenuSecondView : FrameLayout {
    /**
     * 该菜单的类型，由于不同的设备（单光、双光、Lite、TC007、2D编辑）菜单存在差异，用该枚举区分.
     */
    private val menuType: MenuType

    private lateinit var binding: ViewMenuSecondBinding


    /* *********************************************  public 方法  ********************************************* */
    /**
     * 测温: 0-> 拍照      观测 10->拍照
     *
     * 测温: 1-> 点线面
     *
     *                    观测 11->AI识别
     *
     * 测温: 2-> 双光
     *                    观测 13->标靶
     *
     * 测温: 3-> 伪彩颜色   观测 12->伪彩颜色
     *
     * 测温: 4-> 设置
     *
     *                    观测 15->设置
     *
     * 测温: 5-> 温度档位
     *
     *                    观测 14->高低温点
     */
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



    /* *********************************************  public 属性  ********************************************* */

    /**
     * 第 1 个菜单-拍照录像 各个操作的点击事件监听.
     * actionCode: 0-拍照/录像  1-图库  2-更多菜单  3-切换到拍照  4-切换到录像
     */
    var onCameraClickListener: ((actionCode: Int) -> Unit)?
        get() = binding.cameraMenuView.onCameraClickListener
        set(value) {
            binding.cameraMenuView.onCameraClickListener = value
        }
    /**
     * 测温模式-菜单2-点线面 切换事件监听。
     */
    var onFenceListener: ((fenceType: FenceType, isSelected: Boolean) -> Unit)?
        get() = fenceAdapter.onFenceListener
        set(value) {
            fenceAdapter.onFenceListener = value
        }
    /**
     * 测温模式-菜单3-双光 点击事件监听。
     * isSelected: true-切换为选中 false-切换为未选中
     */
    var onTwoLightListener: ((twoLightType: TwoLightType, isSelected: Boolean) -> Unit)?
        get() = twoLightAdapter.onTwoLightListener
        set(value) {
            twoLightAdapter.onTwoLightListener = value
        }
    /**
     * 测温模式-菜单4-伪彩/观测模式-菜单3-伪彩 伪彩切换事件监听.
     * index-选中伪彩在列表中的 index，也就 TC007 要用
     * code-伪彩编码，由于历史遗留跟 index 对不上，非 TC007 时使用
     * size-预设伪彩数量，也就 TC007 要用
     */
    var onColorListener: ((index: Int, code: Int, size: Int) -> Unit)?
        get() = colorAdapter.onColorListener
        set(value) {
            colorAdapter.onColorListener = value
        }
    /**
     * 测温模式-菜单5-设置/观测模式-菜单6-设置 点击事件监听.
     * isSelected: true-点击时为选中状态 false-点击时为未选中状态
     * 警示、字体、水印是以生效才视为高亮选中的，这里先保持旧代码逻辑，
     * 设置菜单的选中刷新丢给上层的 listener 去做，后面有空再考虑更改
     */
    var onSettingListener: ((type: SettingType, isSelected: Boolean) -> Unit)?
        get() = settingTeAdapter.onSettingListener
        set(value) {
            settingTeAdapter.onSettingListener = value
            settingObAdapter.onSettingListener = value
        }
    /**
     * 测温模式-菜单6-高低温档 点击事件监听.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 自动切换：-1
     * - 高温(低增益)：0
     * - 常温(高增益)：1
     */
    var onTempLevelListener: ((code: Int) -> Unit)?
        get() = tempLevelAdapter.onTempLevelListener
        set(value) {
            tempLevelAdapter.onTempLevelListener = value
        }


    /**
     * 观测模式-菜单2-高低温源 点击事件监听.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 什么都未选中：-1
     * - 动态识别：0
     * - 高温源：1
     * - 低温源：2
     */
    var onTempSourceListener: ((code: Int) -> Unit)?
        get() = tempSourceAdapter.onTempSourceListener
        set(value) {
            tempSourceAdapter.onTempSourceListener = value
        }
    /**
     * 观测模式-菜单4-标靶 点击事件监听.
     */
    var onTargetListener: ((targetType: TargetType) -> Unit)?
        get() = targetAdapter.onTargetListener
        set(value) {
            targetAdapter.onTargetListener = value
        }
    /**
     * 观测模式-菜单5-高低温点 点击事件监听.
     */
    var onTempPointListener: ((type: TempPointType, isSelected: Boolean) -> Unit)?
        get() = tempPointAdapter.onTempPointListener
        set(value) {
            tempPointAdapter.onTempPointListener = value
        }





    /**
     * 测温模式-菜单2-点线面 所用 Adapter.
     */
    private val fenceAdapter: FenceAdapter
    /**
     * 测温模式-菜单3-双光 所用 Adapter.
     */
    private val twoLightAdapter: TwoLightAdapter
    /**
     * 测温模式-菜单4-伪彩 or 观测模式-菜单3-伪彩 所用 Adapter.
     */
    private val colorAdapter = ColorAdapter()
    /**
     * 测温模式-菜单5-设置 所用 Adapter.
     */
    private val settingTeAdapter: SettingAdapter
    /**
     * 测温模式-菜单6-高低温档 所用 Adapter.
     */
    private val tempLevelAdapter: TempLevelAdapter


    /**
     * 观测模式-菜单2-高低温源 所用 Adapter.
     */
    private val tempSourceAdapter = TempSourceAdapter()
    /**
     * 观测模式-菜单4-标靶 所用 Adapter.
     */
    private val targetAdapter = TargetAdapter()
    /**
     * 观测模式-菜单5-高低温点 所用 Adapter.
     */
    private val tempPointAdapter = TempPointAdapter()
    /**
     * 观测模式-菜单6-设置 所用 Adapter.
     */
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

            //初始化 测温模式-菜单2-点线面 菜单
            fenceAdapter = FenceAdapter(menuType)
            binding.recyclerFence.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerFence.adapter = fenceAdapter

            //初始化 测温模式-菜单3-双光 菜单
            twoLightAdapter = TwoLightAdapter(menuType)
            binding.recyclerTwoLight.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTwoLight.adapter = twoLightAdapter

            //初始化 测温模式-菜单4-伪彩 or 观测模式-菜单3-伪彩 菜单
            binding.recyclerColor.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerColor.adapter = colorAdapter

            //初始化 测温模式-菜单5-设置 菜单
            settingTeAdapter = SettingAdapter(menuType)
            binding.recyclerSettingTe.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerSettingTe.adapter = settingTeAdapter

            //初始化 测温模式-菜单6-高低温档 菜单
            tempLevelAdapter = TempLevelAdapter(menuType)
            binding.recyclerTempLevel.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTempLevel.adapter = tempLevelAdapter



            //初始化 观测模式-菜单2-高低温源 菜单
            binding.recyclerTempSource.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTempSource.adapter = tempSourceAdapter

            //初始化 观测模式-菜单4-标靶 菜单
            binding.recyclerTarget.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTarget.adapter = targetAdapter

            //初始化 观测模式-菜单5-高低温点 菜单
            binding.recyclerTempPoint.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerTempPoint.adapter = tempPointAdapter

            //初始化 观测模式-菜单6-设置 菜单
            binding.recyclerSettingOb.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.recyclerSettingOb.adapter = settingObAdapter
        }
    }


    /* *********************************************  菜单1-拍照录像  ********************************************* */
    /**
     * 当前是否处于录像模式.
     *
     * true-录像模式 false-拍照模式
     */
    var isVideoMode: Boolean
        get() = binding.cameraMenuView.isVideoMode
        set(value) {
            binding.cameraMenuView.isVideoMode = value
        }

    /**
     * 仅 TS001，测温/观测 切换时，关闭延时拍照、连续拍照、录像后，需要重置为拍照状态.
     */
    fun switchToCamera() {
        binding.cameraMenuView.canSwitchMode = true
        binding.cameraMenuView.isVideoMode = false
        binding.cameraMenuView.setToNormal()
    }

    /**
     * 类似重置，这个方法的目的是重置状态为未拍照、未录像状态，且放开 拍照/录像 切换.
     * 在各个热成像 Activity 的 start()，以及当前 View 中调用
     */
    fun updateCameraModel() {
        binding.cameraMenuView.canSwitchMode = true
        binding.cameraMenuView.setToNormal()
    }

    fun refreshImg(type: GalleryRepository.DirType = GalleryRepository.DirType.LINE) {
        updateCameraModel()//恢复状态
        CoroutineScope(Dispatchers.IO).launch {
            val path = GalleryRepository.readLatest(type)
            launch(Dispatchers.Main) {
                binding.cameraMenuView.refreshGallery(path)
            }
        }
    }

    /**
     * 将中间 拍照/录像 按钮设置为 拍照中-立即/拍照中-延迟/录像中
     */
    fun setToRecord(isDelay: Boolean) {
        binding.cameraMenuView.canSwitchMode = false
        binding.cameraMenuView.setToRecord(isDelay)
    }

    /**
     * 将中间 拍照/录像 按钮设置为 拍照中-立即 状态
     */
    fun setToCamera() {
        binding.cameraMenuView.setToRecord(false)
    }


    /* *****************************************  测温模式-菜单2-点线面  ***************************************** */
    /**
     * 测温模式-菜单2-点线面 当前选中的菜单类型，若为 null 表示所有都未选中.
     */
    var fenceSelectType: FenceType?
        get() = fenceAdapter.selectType
        set(value) {
            fenceAdapter.selectType = value
        }


    /* *****************************************  测温模式-菜单3-双光  ***************************************** */
    /**
     * 当前单选的双光类型
     * - 单光：  不应该使用这个属性
     * - Lite： 不应该使用这个属性
     * - 双光：  双光1、双光2、红外、可见光
     * - TC007：双光、红外、可见光、画中画
     */
    var twoLightType: TwoLightType
        get() = twoLightAdapter.twoLightType
        set(value) {
            twoLightAdapter.twoLightType = value
        }

    /**
     * 设置双光多选状态
     * - 单光：  画中画、融合度
     * - Lite： 画中画、融合度
     * - 双光：  配准、画中画、融合度
     * - TC007：配准、、融合度
     */
    fun setTwoLightSelected(twoLightType: TwoLightType, isSelected: Boolean) {
        twoLightAdapter.setSelected(twoLightType, isSelected)
    }


    /* **********************************  测温模式-菜单4-伪彩/观测模式-菜单3-伪彩  ********************************** */
    /**
     * 根据指定的伪彩代号，选中伪彩菜单中的指定伪彩，若传递的 code 为不支持 code，则为全部未选中效果。
     * @param code 1-白热 3-铁红 4-彩虹1 5-彩虹2 6-彩虹3 7-红热 8-热铁 9-彩虹4 10-彩虹5 11-黑热
     */
    fun setPseudoColor(code: Int) {
        colorAdapter.selectCode = code
    }


    /* **********************************  测温模式-菜单5-设置 or 观测模式-菜单6-设置  ********************************** */
    /**
     * 设置设置菜单中指定选项的选中状态
     */
    fun setSettingSelected(settingType: SettingType, isSelected: Boolean) {
        settingTeAdapter.setSelected(settingType, isSelected)
        settingObAdapter.setSelected(settingType, isSelected)
    }

    /**
     * 设置设置菜单中旋转选项的角度
     * @param rotateAngle 注意！这个值是机芯的旋转角度，非 UI 旋转角度
     */
    fun setSettingRotate(rotateAngle: Int) {
        settingTeAdapter.rotateAngle = rotateAngle
        settingObAdapter.rotateAngle = rotateAngle
    }


    /* *****************************************  测温模式-菜单6-高低温档  ***************************************** */
    /**
     * 温度档位是否使用华氏度作为单位
     *
     * true-华氏度 false-摄氏度
     */
    var isUnitF: Boolean
        get() = tempLevelAdapter.isUnitF
        set(value) {
            tempLevelAdapter.isUnitF = value
        }
    /**
     * 设置 测温模式-菜单6-高低温档 温度档位.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 自动切换：-1
     * - 高温(低增益)：0
     * - 常温(高增益)：1
     */
    fun setTempLevel(code: Int) {
        tempLevelAdapter.selectCode = code
    }






    /* *****************************************  观测模式-菜单2-高低温源  ***************************************** */
    /**
     * 设置 观测模式-菜单2-高低温源 选中.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 什么都未选中：-1
     * - 动态识别：0
     * - 高温源：1
     * - 低温源：2
     */
    fun setTempSource(code: Int) {
        tempSourceAdapter.selectCode = code
    }


    /* *****************************************  观测模式-菜单4-标靶  ***************************************** */
    /**
     * 设置 观测模式-菜单4-标靶 指定选项的选中状态
     */
    fun setTargetSelected(targetType: TargetType, isSelected: Boolean) {
        targetAdapter.setSelected(targetType, isSelected)
    }
    /**
     * 设置 观测模式-菜单4-标靶-测量模式 图标类型.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 人：10
     * - 羊：11
     * - 狗：12
     * - 鸟：13
     */
    fun setTargetMode(modeCode: Int) {
        targetAdapter.setTargetMode(modeCode)
    }

    
    /* *****************************************  观测模式-菜单5-高低温点  ***************************************** */
    /**
     * 设置 观测模式-菜单5-高低温点 菜单中，高温点 或 低稳点 的选中状态。
     */
    fun setTempPointSelect(tempPointType: TempPointType, isSelected: Boolean) {
        tempPointAdapter.setSelected(tempPointType, isSelected)
    }
    /**
     * 清除 观测模式-菜单5-高低温点 菜单的所有选中状态。
     * 这里维持原有逻辑，后续考虑是否直接给选中删除得了。
     */
    fun clearTempPointSelect() {
        tempPointAdapter.clearAllSelect()
    }
}