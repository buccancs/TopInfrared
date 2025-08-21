package com.topdon.module.thermal.ir.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.dao.ThermalDao
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.ktbase.BaseViewModel
import kotlinx.coroutines.*

class IRMonitorViewModel : BaseViewModel() {

    val recordListLD = MutableLiveData<List<ThermalDao.Record>>()

    fun queryRecordList() {
        viewModelScope.launch(Dispatchers.IO) {
            val recordList: List<ThermalDao.Record> = AppDatabase.getInstance().thermalDao().queryRecordList()
            recordListLD.postValue(recordList)
        }
    }





    val detailListLD = MutableLiveData<List<ThermalEntity>>()

    fun queryDetail(startTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val detailList: List<ThermalEntity> = AppDatabase.getInstance().thermalDao().queryDetail(startTime)
            detailListLD.postValue(detailList)
        }
    }

    fun delDetail(startTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().thermalDao().delDetail(startTime)
        }
    }
}