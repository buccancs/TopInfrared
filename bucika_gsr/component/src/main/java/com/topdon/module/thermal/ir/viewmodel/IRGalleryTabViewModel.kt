package com.topdon.module.thermal.ir.viewmodel

import androidx.lifecycle.MutableLiveData
import com.topdon.lib.core.ktbase.BaseViewModel

class IRGalleryTabViewModel : BaseViewModel() {
    /**
     * 是否处于编辑模式.
     */
    val isEditModeLD: MutableLiveData<Boolean> = MutableLiveData(false)
    /**
     * 当前选中数量.
     */
    val selectSizeLD: MutableLiveData<Int> = MutableLiveData(0)

    /**
     * 点击全选的 Fragment index，如 0 表示照片全选，1表示视频全选.
     */
    val selectAllIndex: MutableLiveData<Int> = MutableLiveData(0)
}