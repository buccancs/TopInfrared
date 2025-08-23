package com.topdon.module.thermal.ir.viewmodel

import androidx.lifecycle.MutableLiveData
import com.topdon.lib.core.ktbase.BaseViewModel

class IRGalleryTabViewModel : BaseViewModel() {
    
    val isEditModeLD: MutableLiveData<Boolean> = MutableLiveData(false)
    
    val selectSizeLD: MutableLiveData<Int> = MutableLiveData(0)

    val selectAllIndex: MutableLiveData<Int> = MutableLiveData(0)
}