package com.topdon.module.thermal.ir.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.TimeUtils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.ktbase.BaseViewModel
import com.topdon.lib.core.utils.SingleLiveEvent
import com.topdon.lms.sdk.LMS
import com.topdon.lms.sdk.UrlConstant
import com.topdon.lms.sdk.bean.CommonBean
import com.topdon.lms.sdk.network.HttpProxy
import com.topdon.lms.sdk.network.IResponseCallback
import com.topdon.lms.sdk.network.ResponseBean
import com.topdon.lms.sdk.xutils.http.RequestParams
import com.topdon.module.thermal.ir.report.bean.ReportBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.concurrent.CountDownLatch

class UpReportViewModel : BaseViewModel() {
    val commonBeanLD = SingleLiveEvent<CommonBean>()

    val exceptionLD = SingleLiveEvent<Exception?>()

    fun upload(isTC007: Boolean, reportBean: ReportBean?) {
        viewModelScope.launch {
            uploadImages(reportBean)
            uploadJSON(isTC007, reportBean)
        }
    }

    private suspend fun uploadImages(reportBean: ReportBean?) {
        withContext(Dispatchers.IO) {
            val irList = reportBean?.infrared_data
            if (irList != null) {
                val downLatch = CountDownLatch(irList.size)
                for (reportIrBean in irList) {
                    if (reportIrBean.picture_id.isNotEmpty()) {
                        downLatch.countDown()
                        continue
                    }
                    val file = File(reportIrBean.picture_url)
                    LMS.getInstance().uploadFile(file, 0, 13, 20) {
                        XLog.i(it?.data)
                        if (it?.code == LMS.SUCCESS) {
                            file.delete()
                            val jsonObject = JSONObject(it.data)
                            reportIrBean.picture_id = jsonObject.getString("fileSecret")
                            reportIrBean.picture_url = jsonObject.getString("url")
                        }
                        XLog.i("上传完一张图")
                        downLatch.countDown()
                    }
                }
                downLatch.await()
                XLog.i("${irList.size} 张图上传完毕")
            }
        }
    }

    private suspend fun uploadJSON(isTC007: Boolean, reportBean: ReportBean?) {
        withContext(Dispatchers.IO) {
            val url = UrlConstant.BASE_URL + "api/v1/outProduce/testReport/addTestReport"
            val params = RequestParams()
            params.addBodyParameter("reportType", 2)
            params.addBodyParameter("modelId", if (isTC007) 1783 else 950) //TC001-950, TC002-951, TC003-952 TC007-1783
            params.addBodyParameter("testTime", TimeUtils.getNowString())
            params.addBodyParameter("testInfo", GsonUtils.toJson(reportBean))
            params.addBodyParameter("sn", "")
            HttpProxy.instant.post(url, params, object : IResponseCallback {
                override fun onResponse(response: String?) {
                    commonBeanLD.postValue(ResponseBean.convertCommonBean(response, null))
                }

                override fun onFail(exception: Exception?) {
                    exceptionLD.postValue(exception)
                }
            })
        }
    }
}