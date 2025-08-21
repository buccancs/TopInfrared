package com.topdon.module.thermal.ir.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.elvishew.xlog.XLog
import com.google.android.material.tabs.TabLayoutMediator
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lib.core.repository.GalleryRepository.DirType
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.GalleryDirChangeEvent
import com.topdon.module.thermal.ir.popup.GalleryChangePopup
import com.topdon.module.thermal.ir.popup.OptionPickPopup
import com.topdon.module.thermal.ir.viewmodel.IRGalleryTabViewModel
import kotlinx.android.synthetic.main.fragment_gallery_tab.*
import org.greenrobot.eventbus.EventBus

/**
 * 图库 Tab 页，下分图片和视频.
 *
 * 需要传递参数：
 * - [ExtraKeyConfig.HAS_BACK_ICON] - 图库是否有返回箭头，默认 false
 * - [ExtraKeyConfig.CAN_SWITCH_DIR] - 图库是否可切换 有线设备、TS004、TC007 目录，默认 true
 * - [ExtraKeyConfig.DIR_TYPE] - 进入图库时初始的目录类型 具体取值由 [DirType] 定义
 *
 * Created by chenggeng.lin on 2023/11/14.
 */
class IRGalleryTabFragment : BaseFragment() {
    /**
     * 从上一界面传递过来的，图库是否有返回箭头
     */
    private var hasBackIcon = false
    /**
     * 从上一界面传递过来的，图库是否可切换 有线设备、TS004、TC007 目录
     */
    private var canSwitchDir = true
    /**
     * 从上一界面传递过来的，进入图库时初始的目录类型
     */
    private var currentDirType = DirType.LINE


    private val viewModel: IRGalleryTabViewModel by activityViewModels()

    private var viewPagerAdapter: ViewPagerAdapter? = null

    override fun initContentView(): Int = R.layout.fragment_gallery_tab

    override fun initView() {
        hasBackIcon = arguments?.getBoolean(ExtraKeyConfig.HAS_BACK_ICON, false) ?: false
        canSwitchDir = arguments?.getBoolean(ExtraKeyConfig.CAN_SWITCH_DIR, false) ?: false
        currentDirType = when (arguments?.getInt(ExtraKeyConfig.DIR_TYPE, 0) ?: 0) {
            DirType.TS004_LOCALE.ordinal -> DirType.TS004_LOCALE
            DirType.TS004_REMOTE.ordinal -> DirType.TS004_REMOTE
            DirType.TC007.ordinal -> DirType.TC007
            else -> DirType.LINE
        }

        tv_title_dir.text = when (currentDirType) {
            DirType.LINE -> getString(R.string.tc_has_line_device)
            DirType.TC007 -> "TC007"
            else -> "TS004"
        }
        tv_title_dir.isVisible = canSwitchDir
        tv_title_dir.setOnClickListener {
            val popup = GalleryChangePopup(requireContext())
            popup.onPickListener = { position, str ->
                currentDirType = when (position) {
                    0 -> DirType.LINE
                    1 -> DirType.TS004_LOCALE
                    else -> DirType.TC007
                }
                tv_title_dir.text = str
                EventBus.getDefault().post(GalleryDirChangeEvent(currentDirType))
            }
            popup.show(tv_title_dir)
        }

        title_view.setTitleText(if (canSwitchDir) "" else getString(R.string.app_gallery))
        title_view.setLeftDrawable(if (hasBackIcon) R.drawable.ic_back_white_svg else 0)
        title_view.setLeftClickListener {
            if (viewModel.isEditModeLD.value == true) {//当前为编辑状态，退出编辑
                viewModel.isEditModeLD.value = false
            } else {//当前为非编辑状态，退出页面
                if (hasBackIcon) {
                    requireActivity().finish()
                }
            }
        }
        title_view.setRightDrawable(R.drawable.ic_toolbar_check_svg)
        title_view.setRightClickListener {
            if (viewModel.isEditModeLD.value == true) {//当前为编辑状态，全选
                viewModel.selectAllIndex.value = view_pager2.currentItem
            } else {//当前为非编辑状态，进入编辑
                viewModel.isEditModeLD.value = true
            }
        }

        viewPagerAdapter = ViewPagerAdapter(this)
        view_pager2.adapter = viewPagerAdapter
        TabLayoutMediator(tab_layout, view_pager2) { tab, position ->
            tab.setText(if (position == 0) R.string.album_menu_Photos else R.string.app_video)
        }.attach()

        viewModel.isEditModeLD.observe(viewLifecycleOwner) { isEditMode ->
            if (isEditMode) {
                title_view.setLeftDrawable(R.drawable.svg_x_cc)
            } else {
                title_view.setLeftDrawable(if (hasBackIcon) R.drawable.ic_back_white_svg else 0)
            }
            title_view.setRightDrawable(if (isEditMode) 0 else R.drawable.ic_toolbar_check_svg)
            title_view.setRightText(if (isEditMode) getString(R.string.report_select_all) else "")
            tab_layout.isVisible = !isEditMode
            view_pager2.isUserInputEnabled = !isEditMode
            if (isEditMode) {
                title_view.setTitleText(getString(R.string.chosen_item, viewModel.selectSizeLD.value))
                tv_title_dir.isVisible = false
            } else {
                title_view.setTitleText(if (canSwitchDir) "" else getString(R.string.app_gallery))
                tv_title_dir.isVisible = canSwitchDir
            }
        }
        viewModel.selectSizeLD.observe(viewLifecycleOwner) {
            if (viewModel.isEditModeLD.value == true) {
                title_view.setTitleText(getString(R.string.chosen_item, it))
                tv_title_dir.isVisible = false
            } else {
                title_view.setTitleText(if (canSwitchDir) "" else getString(R.string.app_gallery))
                tv_title_dir.isVisible = canSwitchDir
            }
        }
    }

    override fun initData() {
    }

    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putBoolean(ExtraKeyConfig.IS_VIDEO, position == 1)
            bundle.putInt(ExtraKeyConfig.DIR_TYPE, currentDirType.ordinal)
            val fragment = IRGalleryFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}