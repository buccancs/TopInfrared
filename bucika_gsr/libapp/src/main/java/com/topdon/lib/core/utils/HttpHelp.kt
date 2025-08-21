package com.topdon.lib.core.utils

import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.config.HttpConfig
import com.topdon.lms.sdk.UrlConstant
import com.topdon.lms.sdk.network.HttpProxy.Companion.instant
import com.topdon.lms.sdk.network.IResponseCallback
import com.topdon.lms.sdk.utils.LanguageUtil
import com.topdon.lms.sdk.xutils.http.RequestParams

/**
 * @author: CaiSongL
 * @date: 2023/5/12 17:17
 */
object HttpHelp{

    /**
     * 获取首次报告列表
     * modelId：TC001 是950, TC002 是951, TC003是952
     */
    fun getFirstReportData(isTC007: Boolean, pageNumber:Int,iResponseCallback : IResponseCallback){
        val url = UrlConstant.BASE_URL + "api/v1/outProduce/testReport/getTestReport"
        val params = RequestParams()
        params.addBodyParameter("modelId", if (isTC007) 1783 else 950)//TC001-950, TC002-951, TC003-952 TC007-1783
        params.addBodyParameter("status", 1)
        params.addBodyParameter("reportType", 2)
        params.addBodyParameter("languageId",  LanguageUtil.getLanguageId(Utils.getApp()))
        params.addBodyParameter("current", pageNumber)
        params.addBodyParameter("size", 20)
        instant.post(url,true, params, iResponseCallback)
    }

}