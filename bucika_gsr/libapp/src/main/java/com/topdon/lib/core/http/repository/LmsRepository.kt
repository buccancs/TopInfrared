package com.topdon.lib.core.http.repository

import android.text.TextUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.topdon.lib.core.bean.json.StatementJson
import com.topdon.lib.core.bean.base.Resp
import com.topdon.lib.core.bean.json.CheckVersionJson
import com.topdon.lms.sdk.LMS
import com.topdon.lms.sdk.network.IResponseCallback
import com.topdon.lms.sdk.utils.StringUtils
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

object LmsRepository {

    /**
     * 查看版本信息
     */
    suspend fun getVersionInfo(): CheckVersionJson? {
        var result: CheckVersionJson? = null
        val downLatch = CountDownLatch(1)
        LMS.getInstance().checkAppUpdate {
            try {
                if (it.code == 2000) {
                    result = Gson().fromJson(it.data, CheckVersionJson::class.java)
                }
            } catch (e: Exception) {
                XLog.e("version json解析异常: ${e.message}")
            }
            downLatch.countDown()
        }
        withContext(Dispatchers.IO) {
            downLatch.await()
        }
        return result
    }

    /**
     * 查看声明链接
     */
    suspend fun getStatementUrl(type: String): StatementJson? {
        var result: StatementJson? = null
        val downLatch = CountDownLatch(1)
        LMS.getInstance().getStatement(type, object : IResponseCallback {
            override fun onResponse(p0: String?) {
                try {
                    val typeOfT = object : TypeToken<Resp<StatementJson>>() {}.type
                    val json = Gson().fromJson<Resp<StatementJson>>(p0, typeOfT)
                    if (json.code == "2000") {
                        result = json.data
                    }
                } catch (e: Exception) {
                    XLog.e("json解析异常: ${e.message}")
                }
                downLatch.countDown()
            }

            override fun onFail(p0: Exception?) {
                downLatch.countDown()
                XLog.w("onFail: $result")
            }

            override fun onFail(failMsg: String?, errorCode: String) {
                super.onFail(failMsg, errorCode)
                try {
                    StringUtils.getResString(
                        LMS.mContext,
                        if (TextUtils.isEmpty(errorCode)) -500 else errorCode.toInt()
                    ).let {
                        TToast.shortToast(LMS.mContext, it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        withContext(Dispatchers.IO) {
            downLatch.await()
        }
        return result
    }
}