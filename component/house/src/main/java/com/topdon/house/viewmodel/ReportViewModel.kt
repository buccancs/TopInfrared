package com.topdon.house.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.HouseReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    
    val reportListLD =  MutableLiveData<List<HouseReport>>()
    
    fun queryAll() {
        viewModelScope.launch(Dispatchers.IO) {
            reportListLD.postValue(AppDatabase.getInstance().houseReportDao().queryAllReport())
        }
    }

    val reportLD = MutableLiveData<HouseReport?>()
    
    fun queryById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            reportLD.postValue(AppDatabase.getInstance().houseReportDao().queryById(id))
        }
    }

    fun update(houseReport: HouseReport) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().houseReportDao().updateReport(houseReport)
        }
    }

    fun deleteMore(vararg houseReports: HouseReport) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().houseReportDao().deleteReport(*houseReports)
            for (houseReport in houseReports) {
                FileUtils.delete(File(FileConfig.documentsDir, houseReport.getPdfFileName()))
            }
            queryAll()
        }
    }
}