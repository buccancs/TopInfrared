package com.topdon.module.thermal.ir.viewmodel

import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import com.topdon.lib.core.ktbase.BaseViewModel
import com.topdon.lib.core.utils.ByteUtils.bytesToInt
import com.topdon.lib.core.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class IRGalleryEditViewModel : BaseViewModel() {

    val resultLiveData = SingleLiveEvent<FrameBean>()

    fun initData(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) {
                XLog.w("IR文件不存在: ${file.absolutePath}")
                return@launch
            }
            XLog.w("IR文件: ${file.absolutePath}")
            val bytes = file.readBytes()
            val headLenBytes = ByteArray(2)
            System.arraycopy(bytes, 0, headLenBytes, 0, 2)
            val headLen = headLenBytes.bytesToInt()
            val headDataBytes = ByteArray(headLen)
            val frameDataBytes = ByteArray(bytes.size - headLen)
            System.arraycopy(bytes, 0, headDataBytes, 0, headDataBytes.size)
            System.arraycopy(bytes, headLen, frameDataBytes, 0, frameDataBytes.size)
            XLog.w("一帧数据: ${frameDataBytes.size}")
            resultLiveData.postValue(FrameBean(headDataBytes, frameDataBytes))
        }
    }

    /**
     * 获取尾部信息
     */

    fun getTailData(bytes: ByteArray){

    }



    data class FrameBean(val capital: ByteArray, val frame: ByteArray)

}