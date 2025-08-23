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
            .retryOnConnectionFailure(false)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
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

    suspend fun getProductInfo(): ProductBean? = withContext(Dispatchers.IO) {
        try {
            getTC007Service().getProductInfo().Data
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getBatteryInfo(): BatteryInfo? = withContext(Dispatchers.IO) {
        try {
            getTC007Service().getBatteryInfo().Data
        } catch (_: Exception) {
            null
        }
    }

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

    suspend fun updateFirmware(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val isSendFileSuccess = sendUpgradeFile(file)
            if (!isSendFileSuccess) {
                return@withContext false
            }

            var status = getTC007Service().getUpgradeStatus().Data?.Status
            while (status == 0 || status == 1 || status == 2) {
                delay(1000)
                status = getTC007Service().getUpgradeStatus().Data?.Status
            }

            status == 4
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun sendUpgradeFile(file: File): Boolean = withContext(Dispatchers.IO) {
        val pageSize = 1024 * 1024 * 10
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)

            var result = true
            var packNum = 0
            var hasReadCount = 0
            var byteArray = ByteArray(pageSize)
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
                    if (code == 400805) {
                        return@withContext true
                    }
                    if (code != 200) {
                        result = false
                    }
                    hasReadCount = 0
                    byteArray = ByteArray(pageSize)
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
                if (code == 400805) {
                    return@withContext true
                }
                if (code != 200) {
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

    suspend fun resetToFactory(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTC007Service().resetToFactory().isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun correction(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTC007Service().correction().isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getEnvAttr(): EnvAttr? = withContext(Dispatchers.IO) {
        try {
            getTC007Service().getEnvAttr().Data
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setEnvAttr(isCelsius: Boolean,Level : Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["TempUnit"] = if (isCelsius) 0 else 2
            paramMap["Level"] = Level
            paramMap["Fps"] = 12
            paramMap["OsdMode"] = 1
            paramMap["DistanceUnit"] = 0
            getTC007Service().setEnvAttr(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun setIRConfig(environment: Float, distance: Float, radiation: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["AtmosphereTemp"] = ((environment + 273.15f) * 10).toInt()
            paramMap["Distance"] = (distance * 100).toInt()
            paramMap["Emissivity"] = (radiation * 10000).toInt()
            getTC007Service().setIRConfig(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

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

    suspend fun getPhoto() : TC007Response<PhotoBean>? = withContext(Dispatchers.IO){
        try {
            getTC007Service().getPhoto()
        } catch (e: Exception) {
            XLog.e("请求异常：${e?.message}")
            null
        }
    }

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