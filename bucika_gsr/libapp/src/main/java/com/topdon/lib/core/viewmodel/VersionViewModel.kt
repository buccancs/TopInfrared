package com.topdon.lib.core.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.bean.event.VersionUpData
import com.topdon.lib.core.bean.json.CheckVersionJson
import com.topdon.lib.core.bean.json.SoftConfigOtherTypeVO
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.http.repository.LmsRepository
import com.topdon.lib.core.ktbase.BaseViewModel
import com.topdon.lib.core.tools.VersionTool
import com.topdon.lib.core.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VersionViewModel : BaseViewModel() {

    val updateLiveData = SingleLiveEvent<VersionUpData>()

    fun checkVersion() {
    }

    private fun updateTip(result: CheckVersionJson) {
        val isForcedUpgrade = (result.forcedUpgradeFlag?.toInt() ?: 0) == 1
        val description = getDescription(result.softConfigOtherTypeVOList)
        val downPageUrl = result.downloadPageUrl
        val sizeStr = "${result.notUnZipSize}MB"

        XLog.i("有版本升级,升级信息: $description, 是否强制升级: $isForcedUpgrade")

        val versionUpData = VersionUpData(
            versionNo = result.versionNo ?: "",
            isForcedUpgrade = isForcedUpgrade,
            description = description,
            downPageUrl = downPageUrl,
            sizeStr = sizeStr
        )
        updateLiveData.postValue(versionUpData)
    }

    private fun getDescription(list: List<SoftConfigOtherTypeVO>?): String {
        list?.forEach {
            if (it.descType == 3) {
                return it.textDescription
            }
        }
        return ""
    }

}