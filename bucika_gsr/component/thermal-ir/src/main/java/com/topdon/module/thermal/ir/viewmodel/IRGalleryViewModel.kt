package com.topdon.module.thermal.ir.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topdon.lib.core.bean.GalleryBean
import com.topdon.lib.core.bean.GalleryTitle
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.ktbase.BaseViewModel
import com.topdon.lib.core.repository.GalleryRepository
import com.topdon.lib.core.repository.TS004Repository
import com.topdon.lib.core.tools.TimeTool
import com.topdon.module.thermal.ir.utils.WriteTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class IRGalleryViewModel : BaseViewModel() {
    companion object {
        /**
         * 分页加载时 1 页数据的条数
         */
        const val PAGE_COUNT = 20
    }

    /**
     * 未掺杂日期标题的原始数据列表.
     */
    val sourceListLD: MutableLiveData<ArrayList<GalleryBean>> = MutableLiveData()
    /**
     * 添加了日期标题的用于显示的列表.
     */
    val showListLD: MutableLiveData<ArrayList<GalleryBean>> = MutableLiveData()

    /**
     * 仅供生成报告使用的，加载所有插件式设备图片.
     */
    fun queryAllReportImg(dirType: GalleryRepository.DirType) {
        viewModelScope.launch(Dispatchers.IO) {
            val sourceList: ArrayList<GalleryBean> = GalleryRepository.loadAllReportImg(dirType)
            sourceListLD.postValue(sourceList)

            //插入日期 item
            val showList: ArrayList<GalleryBean> = ArrayList(sourceList.size)
            var beforeTime = 0L
            for (galleryBean in sourceList) {
                val currentTime = TimeTool.timeToMinute(galleryBean.timeMillis, 4)
                if (beforeTime != currentTime) {//新的日期
                    showList.add(GalleryTitle(galleryBean.timeMillis))
                    beforeTime = currentTime
                }
                showList.add(galleryBean)
            }
            showListLD.postValue(showList)
        }
    }

    /**
     * 分页加载时已成功加载的页数
     */
    var hasLoadPage = 0
    /**
     * 一页请求数据列表.
     * null-请求失败
     */
    val pageListLD: MutableLiveData<ArrayList<GalleryBean>?> = MutableLiveData()

    fun queryGalleryByPage(isVideo: Boolean, dirType: GalleryRepository.DirType) {
        viewModelScope.launch(Dispatchers.IO) {
            val pageList: ArrayList<GalleryBean>? = GalleryRepository.loadByPage(isVideo, dirType, hasLoadPage + 1, PAGE_COUNT)
            pageListLD.postValue(pageList)

            if (pageList != null) {
                val sourceList = if (hasLoadPage == 0) ArrayList(pageList.size) else sourceListLD.value ?: ArrayList(pageList.size)
                val showList = if (hasLoadPage == 0) ArrayList(pageList.size) else showListLD.value ?: ArrayList(pageList.size)
                if (pageList.isNotEmpty()) {
                    hasLoadPage++
                }

                //插入日期 item
                var beforeTime = if (sourceList.isEmpty()) 0 else TimeTool.timeToMinute(sourceList.last().timeMillis, 4)
                for (galleryBean in pageList) {
                    val currentTime = TimeTool.timeToMinute(galleryBean.timeMillis, 4)
                    if (beforeTime != currentTime) {//新的日期
                        showList.add(GalleryTitle(galleryBean.timeMillis))
                        beforeTime = currentTime
                    }
                    showList.add(galleryBean)
                }

                sourceList.addAll(pageList)
                sourceListLD.postValue(sourceList)
                showListLD.postValue(showList)
            }
        }
    }





    /**
     * 批量删除文件结果.
     */
    val deleteResultLD: MutableLiveData<Boolean> = MutableLiveData()

    fun delete(deleteList: List<GalleryBean>, dirType: GalleryRepository.DirType, isDelLocal: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (dirType == GalleryRepository.DirType.TS004_REMOTE) {
                val isSuccess = TS004Repository.deleteFiles(Array(deleteList.size) {
                    deleteList[it].id
                })
                if (isSuccess) {
                    if (isDelLocal) {
                        deleteList.forEach {
                            if (it.hasDownload) {
                                val file = File(FileConfig.ts004GalleryDir, it.name)
                                if (file.exists()) {
                                    WriteTools.delete(file)
                                }
                            }
                        }
                    }
                    deleteResultLD.postValue(true)
                } else {
                    deleteResultLD.postValue(false)
                }
            } else {
                deleteList.forEach {
                    val file = File(it.path)
                    if (file.exists()) {
                        WriteTools.delete(file)
                    }
                }
                deleteResultLD.postValue(true)
            }
        }
    }
}