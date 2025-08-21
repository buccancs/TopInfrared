package com.topdon.lib.core.repository

import android.graphics.Point
import android.graphics.Rect
import android.net.Network
import com.blankj.utilcode.util.EncryptUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.topdon.lib.core.http.converter.StringConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TC007Repository {

    private fun Any.toBody(): RequestBody = Gson().toJson(this).toRequestBody()

    var netWork: Network? = null
    var tempFrameParam : TempFrameParam ?= null

    private fun getOKHttpClient(timeout: Long): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)//不重试
            .connectTimeout(timeout, TimeUnit.SECONDS)//2024-5-29 TS004 群中决定接口统一超时15秒
            .readTimeout(timeout, TimeUnit.SECONDS)   //2024-5-29 TS004 群中决定接口统一超时15秒
            .writeTimeout(timeout, TimeUnit.SECONDS)  //2024-5-29 TS004 群中决定接口统一超时15秒
            .addInterceptor(OKLogInterceptor(true))
        netWork?.socketFactory?.let {
            builder.socketFactory(it)
        }
        return builder.build()
    }

    private fun getTC007Service(timeout: Long = 15): TC007Service = Retrofit.Builder()
        .baseUrl("http://192.168.40.1:63206")
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(StringConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(getOKHttpClient(timeout))
        .build()
        .create(TC007Service::class.java)





    /**
     * 获取产品信息
     */
    suspend fun getProductInfo(): ProductBean? = withContext(Dispatchers.IO) {
        try {
            getTC007Service().getProductInfo().Data
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 获取设备电池信息
     */
    suspend fun getBatteryInfo(): BatteryInfo? = withContext(Dispatchers.IO) {
        try {
            getTC007Service().getBatteryInfo().Data
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 同步时间.
     */
    suspend fun syncTime(): Boolean = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["Year"] = calendar.get(Calendar.YEAR)
            paramMap["Month"] = calendar.get(Calendar.MONTH) + 1
            paramMap["Day"] = calendar.get(Calendar.DAY_OF_MONTH)
            paramMap["Hour"] = calendar.get(Calendar.HOUR_OF_DAY)
            paramMap["Minute"] = calendar.get(Calendar.MINUTE)
            paramMap["Second"] = calendar.get(Calendar.SECOND)
            getTC007Service().syncTime(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }


    /**
     * 执行固件升级.
     */
    suspend fun updateFirmware(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val isSendFileSuccess = sendUpgradeFile(file)
            if (!isSendFileSuccess) {
                return@withContext false
            }

            var status = getTC007Service().getUpgradeStatus().Data?.Status
            while (status == 0 || status == 1 || status == 2) {//文档跟实际值对不上
                delay(1000)
                status = getTC007Service().getUpgradeStatus().Data?.Status
            }

            status == 4
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun sendUpgradeFile(file: File): Boolean = withContext(Dispatchers.IO) {
        val pageSize = 1024 * 1024 * 10 //10M每包
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)

            var result = true
            var packNum = 0
            var hasReadCount = 0
            var byteArray = ByteArray(pageSize)//10M每包
            val totalPackNum = (file.length() / (pageSize) + (if (file.length() % (pageSize) > 0) 1 else 0)).toInt()
            val md5 = EncryptUtils.encryptMD5File2String(file).lowercase(Locale.ROOT)

            var readCount = fileInputStream.read(byteArray)
            while (readCount != -1) {
                hasReadCount += readCount
                if (hasReadCount == pageSize) {
                    packNum++
                    val body = byteArray.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("zipFile", "zipFile", body)
                    val code = getTC007Service(30).sendUpgradeFile(file.name, packNum, totalPackNum, md5, part).Code
                    if (code == 400805) {//已在升级中
                        return@withContext true
                    }
                    if (code != 200) {//200是成功
                        result = false
                    }
                    hasReadCount = 0
                    byteArray = ByteArray(pageSize)//10M每包
                }
                readCount = fileInputStream.read(byteArray, hasReadCount, byteArray.size - hasReadCount)
            }

            if (hasReadCount > 0) {
                packNum++
                val lastArray = ByteArray(hasReadCount)
                System.arraycopy(byteArray, 0, lastArray, 0, hasReadCount)
                val body = lastArray.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("zipFile", "zipFile", body)
                val code = getTC007Service(30).sendUpgradeFile(file.name, packNum, totalPackNum, md5, part).Code
                if (code == 400805) {//已在升级中
                    return@withContext true
                }
                if (code != 200) {//200是成功
                    result = false
                }
            }

            result
        } catch (e: Exception) {
            false
        } finally {
            fileInputStream?.close()
        }
    }

    /**
     * 恢复出厂设置
     */
    suspend fun resetToFactory(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTC007Service().resetToFactory().isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 执行锅盖标定
     */
    suspend fun correction(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTC007Service().correction().isSuccess()
        } catch (_: Exception) {
            false
        }
    }


    /**
     * 获取测温属性参数
     */
    suspend fun getEnvAttr(): EnvAttr? = withContext(Dispatchers.IO) {
        try {
            getTC007Service().getEnvAttr().Data
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 设置温度单位是否为摄氏度
     * @param isCelsius true-摄氏度 false-华氏度
     * @param Level 测温档位,0:高增益 1:低增益 3:自动切换
     */
    suspend fun setEnvAttr(isCelsius: Boolean,Level : Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["TempUnit"] = if (isCelsius) 0 else 2 //0-摄氏度 1-开尔文 2-华氏度
            paramMap["Level"] = Level //0:高增益 1:低增益 3:自动切换
            paramMap["Fps"] = 12 //测温帧率,范围[0,采集帧率],默认12,最高支持12帧
            paramMap["OsdMode"] = 1 //测温信息叠加方式，0:视频编码前叠加 1:码流信息叠加(编码后预览时叠加) 2:无叠加
            paramMap["DistanceUnit"] = 0 //距离单位，0:米 1:英尺
            getTC007Service().setEnvAttr(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 设置温度修正参数
     * @param environment 环境温度，单位摄氏度
     * @param distance 测温距离，单位米
     * @param radiation 发射率 `[0.01,1]`
     */
    suspend fun setIRConfig(environment: Float, distance: Float, radiation: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["AtmosphereTemp"] = ((environment + 273.15f) * 10).toInt()
            paramMap["Distance"] = (distance * 100).toInt()
            paramMap["Emissivity"] = (radiation * 10000).toInt()
//            paramMap["ReflectedTemp"] = 2982
//            paramMap["Transmittance"] = 10000
            getTC007Service().setIRConfig(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 清除所有点、线、面.
     */
    suspend fun clearAllTemp(): Boolean = withContext(Dispatchers.IO) {
        try {
            var result = true
            result = result && setTempPointList(ArrayList())
            result = result && setTempLineList(ArrayList())
            result = result && setTempRectList(ArrayList())
            result
        } catch (_: Exception) {
            false
        }
    }
    /**
     * 切换全局测温开关状态
     */
    suspend fun getTempFrame(): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = getTC007Service().getTempFrame()
            if (data.isSuccess()){
                tempFrameParam = data.Data
            }
            data.isSuccess()
        } catch (_: Exception) {
            false
        }
    }
    /**
     * 设置全局测温开启或关闭.
     */
    suspend fun setTempFrame(boolean: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            if (tempFrameParam != null){
                tempFrameParam!!.FrameLow.Enable = boolean
                tempFrameParam!!.FrameCenter.Enable = boolean
                tempFrameParam!!.FrameHigh.Enable = boolean
                getTC007Service().setTempFrame(tempFrameParam!!.toBody()).isSuccess()
            }else{
                false
            }
        } catch (_: Exception) {
            false
        }
    }
    /**
     * 设置测温点列表.
     */
    suspend fun setTempPointList(pointList: List<Point>): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramList = ArrayList<TempPointParam>(3)
            for (i in 0 until 3) {
                paramList.add(TempPointParam(i + 1, if (i < pointList.size) pointList[i] else null))
            }
            getTC007Service().setTempPoint(paramList.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }
    /**
     * 设置测温线列表.
     */
    suspend fun setTempLineList(lineList: List<Point>): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramList = ArrayList<TempLineParam>(3)
            for (i in 0 until 3) {
                val startPoint = if (i * 2 < lineList.size) lineList[i * 2] else null
                val endPoint = if (i * 2 + 1 < lineList.size) lineList[i * 2 + 1] else null
                paramList.add(TempLineParam(i + 1, startPoint, endPoint))
            }
            getTC007Service().setTempLine(paramList.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }
    /**
     * 设置测温面列表.
     */
    suspend fun setTempRectList(rectList: List<Rect>): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramList = ArrayList<TempRectParam>(3)
            for (i in 0 until 3) {
                paramList.add(TempRectParam(i + 1, if (i < rectList.size) rectList[i] else null))
            }
            getTC007Service().setTempRect(paramList.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }


    /**
     * 拍照
     */
    suspend fun getPhoto() : TC007Response<PhotoBean>? = withContext(Dispatchers.IO){
        try {
            getTC007Service().getPhoto()
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

    /**
     * 设置图像模式
     */
    suspend fun setMode(mode : Int) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        try {
            getTC007Service().setMode(mode)
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

    suspend fun getAttribute(default : Boolean) : TC007Response<AttributeBean?>? = withContext(Dispatchers.IO){
        try {
            getTC007Service().getAttribute(1,default.toString())
        } catch (e: Exception) {
            null
        }
    }



    suspend fun setRatio(data: Any?) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        if (data == null){
            return@withContext null
        }
        try {
            getTC007Service().setRatio(data.toBody())
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }


    suspend fun getRegistration(default : Boolean) : TC007Response<WifiAttributeBean?>? = withContext(Dispatchers.IO){
        try {
            getTC007Service().getRegistration(1,default.toString())
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setRegistration(data: Any?) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        if (data == null){
            return@withContext null
        }
        try {
            getTC007Service().setRegistration(data.toBody())
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }


    suspend fun setPallete(data: Any?) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        if (data == null){
            return@withContext null
        }
        try {
            getTC007Service().setPallete(data.toBody())
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

    suspend fun setParam(data: Any?) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        if (data == null){
            return@withContext null
        }
        try {
            getTC007Service().setParam(data.toBody())
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

    suspend fun setFont(data: Any?) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        if (data == null){
            return@withContext null
        }
        try {
            getTC007Service().setFont(data.toBody())
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

    suspend fun setCorrection() : TC007Response<Any?>? = withContext(Dispatchers.IO){
        try {
            getTC007Service().setCorrection()
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

    suspend fun setIsotherm(data: Any?) : TC007Response<Any?>? = withContext(Dispatchers.IO){
        if (data == null){
            return@withContext null
        }
        try {
            getTC007Service().setIsotherm(data.toBody())
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }
}