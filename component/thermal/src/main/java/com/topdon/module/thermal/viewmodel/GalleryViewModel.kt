package com.topdon.module.thermal.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.config.FileConfig.galleryPath
import com.topdon.lib.core.ktbase.BaseViewModel
import com.topdon.lib.core.utils.SingleLiveEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class GalleryViewModel : BaseViewModel() {

    val galleryLiveData = SingleLiveEvent<ArrayList<String>>()

    fun getData() {
        viewModelScope.launch {
            getGalleryList().collect { it ->
                if (it.size == 0) {
                    Log.w("123", "文件不存在")
                } else {
                    galleryLiveData.postValue(it)
                }
            }
        }
    }

    fun getVideoData() {
        viewModelScope.launch {
            getVideoList().collect { it ->
                if (it.size == 0) {
                    Log.w("123", "文件不存在")
                } else {
                    galleryLiveData.postValue(it)
                }
            }
        }
    }

    private fun getGalleryList(): Flow<ArrayList<String>> {
        val flow = flow {
            val path = Utils.getApp()
                .getExternalFilesDir("Pictures")!!.absolutePath + File.separator + "thermal"
            val file = File(path)
            if (file.isDirectory) {
                val list = arrayListOf<String>()
                file.list().forEach {
                    list.add("${path}/${it}")
                }
                emit(list)
            } else {
                emit(arrayListOf<String>())
            }
        }.map {
            return@map it
        }
        return flow
    }

    private fun getVideoList(): Flow<ArrayList<String>> {
        val flow = flow {
            val path = galleryPath
            val file = File(path)
            if (file.isDirectory) {
                val list = arrayListOf<String>()
                file.list().forEach {
                    list.add("${path}/${it}")
                }
                emit(list)
            } else {
                emit(arrayListOf<String>())
            }
        }.map {
            return@map it
        }
        return flow
    }
}