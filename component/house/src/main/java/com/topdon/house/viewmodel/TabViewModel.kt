package com.topdon.house.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

internal class TabViewModel(application: Application) : AndroidViewModel(application) {
    
    val isEditModeLD: MutableLiveData<Boolean> = MutableLiveData(false)
    
    val selectSizeLD: MutableLiveData<Int> = MutableLiveData(0)
}