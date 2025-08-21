package com.topdon.lib.core.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.topdon.lib.core.R
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.repository.DeviceInfo
import com.topdon.lib.core.repository.ProductBean
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.repository.TS004Repository
import com.topdon.lms.sdk.LMS
import com.topdon.lms.sdk.UrlConstant
import com.topdon.lms.sdk.bean.CommonBean
import com.topdon.lms.sdk.network.HttpProxy
import com.topdon.lms.sdk.network.IResponseCallback
import com.topdon.lms.sdk.network.ResponseBean
import com.topdon.lms.sdk.utils.DateUtils
import com.topdon.lms.sdk.utils.LanguageUtil
import com.topdon.lms.sdk.xutils.http.RequestParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.NumberFormatException
import java.util.TimeZone
import java.util.concurrent.CountDownLatch

/**
 * 固件升级包
 */
class FirmwareViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        /**
         * TS004 固件升级包 软件编码.
         */
        private const val TS004_SOFT_CODE = "TS004_FirmwareSW_Scope"
        /**
         * TC007 固件升级包 软件编码.
         */
        private const val TC007_SOFT_CODE = "TC007_FirmwareSW_Wireless"

        /**
         * TS004 apk 内置固件升级包版本.
         */
        private const val TS004_FIRMWARE_VERSION = "V1.70"
        /**
         * TS004 apk 内置固件升级包文件名.
         */
        private const val TS004_FIRMWARE_NAME = "TS004V1.70.zip"

        /**
         * TC007 apk 内置固件升级包版本.
         */
        private const val TC007_FIRMWARE_VERSION = "V4.06"
        /**
         * TC007 apk 内置固件升级包文件名.
         */
        private const val TC007_FIRMWARE_NAME = "TC007V4.06.zip"


        private const val USE_DEBUG_SN = false
        private const val TS004_DEBUG_SN = "1D003655A10016"
        private const val TS004_DEBUG_RANDOM_NUM = "8D2N01"
        private const val TC007_DEBUG_SN = "1D004714E10002"
        private const val TC007_DEBUG_RANDOM_NUM = "EN6L6Q"
    }

    /**
     * 用一个变量来存储请求状态，避免重复请求.
     */
    @Volatile
    private var isRequest = false



    /**
     * 查询固件升级包成功 LiveData.
     * null表示查询成功但没有配固件升级包
     */
    val firmwareDataLD: MutableLiveData<FirmwareData?> = MutableLiveData()
    /**
     * 查询固件升级包失败 LiveData.
     * true-设备已被其他用户绑定错误 false-普通错误
     */
    val failLD: MutableLiveData<Boolean> = MutableLiveData()


    /**
     * 一个固件升级包信息.
     * @param version 该固件升级包版本，V1.00格式
     * @param updateStr 升级文案信息
     * @param downUrl 固件升级包 URL
     * @param size 固件升级包大小，单位 byte
     */
    data class FirmwareData(
        val version: String,
        val updateStr: String,
        val downUrl: String,
        val size: Long,
    )


    /**
     * 执行一次固件升级包查询，结果发送往：
     * - [firmwareDataLD] (成功)
     * - [failLD] (失败)
     * @param isTS004 true-TS004 false-TC007
     */
    fun queryFirmware(isTS004: Boolean) {
        if (isRequest) {//别催别催，在查了
            return
        }
        isRequest = true

        viewModelScope.launch(Dispatchers.IO) {
            //由于双通道方案存在问题，V3.30临时使用 apk 内置固件升级包，以下使用网络的代码先注释
            /*if (isTS004) {
                //从 TS004 中获取 SN、激活码
                val deviceInfo: DeviceInfo? = TS004Repository.getDeviceInfo()?.data
                if (deviceInfo == null) {
                    XLog.w("TS004 固件升级 - 从设备查询 SN、激活码 失败!")
                    failLD.postValue(false)
                    isRequest = false
                    return@launch
                }

                //从 TS004 中获取固件版本
                val firmware: String? = TS004Repository.getVersion()?.data?.firmware
                if (firmware == null) {
                    XLog.w("TS004 固件升级 - 从设备查询 固件版本 失败!")
                    failLD.postValue(false)
                    isRequest = false
                    return@launch
                }

                val sn: String = if (USE_DEBUG_SN) TS004_DEBUG_SN else deviceInfo.sn
                val randomNum: String = if (USE_DEBUG_SN) TS004_DEBUG_RANDOM_NUM else deviceInfo.code
                getInfoFromNetwork(true, sn, randomNum, firmware)
            } else {
                //从 TC007 中获取 SN、激活码
                val productInfo: ProductBean? = TC007Repository.getProductInfo()
                if (productInfo == null) {
                    XLog.w("TC007 固件升级 - 从设备查询 SN、激活码 失败!")
                    failLD.postValue(false)
                    isRequest = false
                    return@launch
                }

                val sn: String = if (USE_DEBUG_SN) TC007_DEBUG_SN else productInfo.ProductSN
                val randomNum: String = if (USE_DEBUG_SN) TC007_DEBUG_RANDOM_NUM else productInfo.Code
                val firmware = "V${productInfo.getVersionStr()}"
                getInfoFromNetwork(false, sn, randomNum, firmware)
            }*/


            //由于双通道方案存在问题，V3.30临时使用 apk 内置固件升级包，以下为临时方案逻辑
            if (isTS004) {
                //从 TS004 中获取固件版本
                val firmware: String? = TS004Repository.getVersion()?.data?.firmware
                if (firmware == null) {
                    XLog.w("TS004 固件升级 - 从设备查询 固件版本 失败!")
                    failLD.postValue(false)
                    isRequest = false
                    return@launch
                }

                getInfoFromAssets(true, firmware)
            } else {
                //从 TC007 中获取固件版本
                val productInfo: ProductBean? = TC007Repository.getProductInfo()
                if (productInfo == null) {
                    XLog.w("TC007 固件升级 - 从设备查询 SN、激活码 失败!")
                    failLD.postValue(false)
                    isRequest = false
                    return@launch
                }

                getInfoFromAssets(false, "V${productInfo.getVersionStr()}")
            }
        }
    }

    /**
     * 将 assets 中的固件升级包导出，并将相关信息 post 到对应 LiveData
     */
    private fun getInfoFromAssets(isTS004: Boolean, firmware: String) {
        val apkVersionStr = if (isTS004) TS004_FIRMWARE_VERSION else TC007_FIRMWARE_VERSION
        val apkFirmwareName = if (isTS004) TS004_FIRMWARE_NAME else TC007_FIRMWARE_NAME

        val newVersion: Double = getVersionFromStr(apkVersionStr)
        val currentVersion: Double = getVersionFromStr(firmware)
        XLog.d("${if (isTS004) "TS004" else "TC007"} 固件升级 - 当前版本：$currentVersion apk内置版本：$newVersion")
        if (newVersion <= currentVersion) {//当前固件升级包已是最新
            firmwareDataLD.postValue(null)
            isRequest = false
            return
        }

        val firmwareFile = FileConfig.getFirmwareFile(apkFirmwareName)
        try {
            val application: Application = getApplication()
            val inputStream = application.assets.open(apkFirmwareName)
            val outputStream: OutputStream = FileOutputStream(firmwareFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            XLog.e("${if (isTS004) "TS004" else "TC007"} 固件升级 - 导出内置固件升级包失败! ${e.message}")
            FileUtils.delete(firmwareFile)
            firmwareDataLD.postValue(null)
            isRequest = false
            return
        }

        //需求就是只需要中英两种语言，其他语言就使用英文。
        val tipsStr = getApplication<Application>().getString(R.string.fireware_update_tips)

        firmwareDataLD.postValue(FirmwareData(apkVersionStr, tipsStr, apkFirmwareName, firmwareFile.length()))
        isRequest = false
    }

    /**
     * 调接口走完整的获取固件升级包信息流程.
     */
    private suspend fun getInfoFromNetwork(isTS004: Boolean, sn: String, randomNum: String, firmware: String) {
        //绑定设备
        val bindCode = bindDevice(sn, randomNum)
        if (bindCode != LMS.SUCCESS && bindCode != 15109) {
            XLog.w("${if (isTS004) "TS004" else "TC007"} 固件升级 - 绑定设备失败! sn: $sn")
            failLD.postValue(bindCode == 15162)
            isRequest = false
            return
        }

        //获取固件升级包列表
        val packageData: PackageData? = querySoftPackage(sn, if (isTS004) TS004_SOFT_CODE else TC007_SOFT_CODE)
        if (packageData == null) {
            XLog.w("${if (isTS004) "TS004" else "TC007"} 固件升级 - 获取固件升级包信息失败!")
            failLD.postValue(false)
            isRequest = false
            return
        }

        val record: PackageData.Record? = packageData.getFirstRecord()
        val newVersionStr: String? = record?.maxUpdateVersion
        if (record == null || newVersionStr == null) {//没有固件升级包，即当前固件已是最新
            XLog.d("${if (isTS004) "TS004" else "TC007"} 固件升级 - 没有固件升级包，即当前固件已是最新")
            firmwareDataLD.postValue(null)
            isRequest = false
            return
        }

        val newVersion: Double = getVersionFromStr(newVersionStr)
        val currentVersion: Double = getVersionFromStr(firmware)
        XLog.d("${if (isTS004) "TS004" else "TC007"} 固件升级 - 当前版本：$currentVersion 服务器版本：$newVersion")
        if (newVersion <= currentVersion) {//当前固件升级包已是最新
            firmwareDataLD.postValue(null)
            isRequest = false
            return
        }

        //获取固件升级包下载地址
        val downloadData = queryDownloadUrl(sn, record.maxUpdateVersionSoftId)
        if (downloadData?.responseCode == LMS.SUCCESS) {
            firmwareDataLD.postValue(
                FirmwareData(
                    newVersionStr,
                    record.getUpdateStr(),
                    downloadData.downUrl ?: "",
                    downloadData.size ?: 0,
                )
            )
        } else {
            XLog.w("${if (isTS004) "TS004" else "TC007"} 固件升级 - 获取固件包下载地址失败!")
            failLD.postValue(downloadData?.responseCode == 60312)
        }
        isRequest = false
    }

    /**
     * 将设备 SN、注册码与当前账号绑定.
     */
    private suspend fun bindDevice(sn: String, randomNum: String): Int {
        return withContext(Dispatchers.IO) {
            var code = LMS.SUCCESS
            val countDownLatch = CountDownLatch(1)
            LMS.getInstance().bindDevice(sn, randomNum, "", "") {
                code = it.code
                countDownLatch.countDown()
            }
            countDownLatch.await()
            return@withContext code
        }
    }

    /**
     * 查询指定 SN 的固件升级包列表
     */
    private suspend fun querySoftPackage(sn: String, softCode: String): PackageData? = withContext(Dispatchers.IO) {
        var packageData: PackageData? = null
        val countDownLatch = CountDownLatch(1)

        val url = UrlConstant.BASE_URL + "api/v1/user/deviceSoftOut/page"
        val params = RequestParams()
        params.addBodyParameter("sn", sn)
        params.addBodyParameter("softCode", softCode)
        params.addBodyParameter("downloadLanguageId", LanguageUtil.getLanguageId(Utils.getApp()))
        params.addBodyParameter("downloadPlatformId", 2) //1-IOS 2-APP 3-官网 4-PC 5-生产 6-其他
        params.addBodyParameter("queryTime", DateUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT")))
        HttpProxy.instant.post(url, params, object : IResponseCallback {
            override fun onResponse(response: String?) {
                try {
                    val commonBean: CommonBean = ResponseBean.convertCommonBean(response, null)
                    packageData = Gson().fromJson(commonBean.data, PackageData::class.java)
                } catch (_: Exception) {

                }
                countDownLatch.countDown()
            }

            override fun onFail(exception: Exception?) {
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()
        return@withContext packageData
    }

    /**
     * 查询指定 SN 指定固件升级包的下载信息.
     */
    private suspend fun queryDownloadUrl(sn: String, businessId: Int): DownloadData? = withContext(Dispatchers.IO) {
        var result: DownloadData? = null
        val countDownLatch = CountDownLatch(1)
        val url = UrlConstant.BASE_URL + "api/v1/user/deviceSoftOut/getFileUrl"
        val params = RequestParams()
        params.addBodyParameter("sn", sn)
        params.addBodyParameter("businessId", businessId)
        params.addBodyParameter("businessType", 20)//业务类型，20-软件包
        params.addBodyParameter("productType", 20)//0-未知 10-贸易体系 20-品牌体系
        params.addBodyParameter("isCheckPoint", 0)//0-不校验 1-校验（也不知道校验的是什么，接口文档没说）
        HttpProxy.instant.post(url, params, object : IResponseCallback {
            override fun onResponse(response: String?) {
                try {
                    val commonBean: CommonBean = ResponseBean.convertCommonBean(response, null)
                    if (commonBean.code == LMS.SUCCESS) {
                        result = Gson().fromJson(commonBean.data, DownloadData::class.java)
                        result?.responseCode = commonBean.code
                    } else {
                        result = DownloadData("", 0, commonBean.code)
                    }
                } catch (_: Exception) {

                }
                countDownLatch.countDown()
            }

            override fun onFail(exception: Exception?) {
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return@withContext result
    }

    private fun getVersionFromStr(versionStr: String): Double = try {
        if (versionStr[0] == 'V') {
            versionStr.substring(1, versionStr.length).toDouble()
        } else {
            versionStr.toDouble()
        }
    } catch (e: NumberFormatException) {
        0.0
    }


    /**
     * 用来解析 获取固件升级包列表 接口返回的数据.
     */
    private class PackageData {
        var records: List<Record>? = null

        fun getFirstRecord(): Record? = if (records?.isNotEmpty() == true) records?.get(0) else null

        data class Record(
            var maxUpdateVersion: String?,   //版本名，如"V1.32"
            var maxUpdateVersionSoftId: Int,//仅用来请求对应URL
            var maxVersionDetailResVO: MaxVersionDetailResVO?,
        ) {

            fun getUpdateStr(): String {
                val otherExplain: List<OtherExplain>? = maxVersionDetailResVO?.otherExplain
                if (otherExplain != null) {
                    for (data in otherExplain) {
                        if (data.valueType == 3) {
                            return data.textDescription ?: ""
                        }
                    }
                }
                return ""
            }
        }

        data class MaxVersionDetailResVO(
            val otherExplain: List<OtherExplain>?,
        )

        data class OtherExplain(
            val valueType: Int,//1-软件名称 2-软件介绍 3-更新说明 4-注意事项
            val textDescription: String?,
        )
    }

    /**
     * 用来解析 获取固件升级包对应下载信息 接口返回数据.
     */
    private data class DownloadData(
        val downUrl: String?,
        val size: Long?,
        var responseCode: Int,
    )
}