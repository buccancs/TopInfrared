package com.topdon.lib.core.repository

import android.net.Network
import com.blankj.utilcode.util.EncryptUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.topdon.lib.core.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object TS004Repository {
    private fun Any.toBody(): RequestBody = Gson().toJson(this).toRequestBody()

    var netWork : Network ?= null
    private fun getOKHttpClient(): OkHttpClient  {
        val build = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(OKLogInterceptor(false))
        netWork?.socketFactory?.let {
            build.socketFactory(it)
        }

        return build.build()
    }

    private fun getTS004Service(): TS004Service = Retrofit.Builder()
        .baseUrl("http://192.168.40.1:8080")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(getOKHttpClient())
        .build()
        .create(TS004Service::class.java)

    suspend fun downloadList(dataMap: Map<String, File>, listener: ((path: String, isSuccess: Boolean) -> Unit)): Int {
        return withContext(Dispatchers.IO) {
            var successCount = 0
            dataMap.forEach {
                val isSuccess = download(it.key, it.value)
                launch(Dispatchers.Main) {
                    listener.invoke(it.key, isSuccess)
                }
                if (isSuccess) {
                    successCount++
                }
            }
            return@withContext successCount
        }
    }

    suspend fun download(url: String, file: File): Boolean = withContext(Dispatchers.IO) {
        val responseBody = try {
            getTS004Service().download(url)
        } catch (_: Exception) {
            return@withContext false
        }
        var inputStream: InputStream? = null
        var fileOutputString: FileOutputStream? = null
        try {
            inputStream = responseBody.byteStream()
            fileOutputString = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var readLength = inputStream.read(buffer)
            while (readLength != -1) {
                fileOutputString.write(buffer, 0, readLength)
                readLength = inputStream.read(buffer)
            }
            fileOutputString.flush()

            return@withContext true
        } catch (_: Exception) {
            return@withContext false
        } finally {
            inputStream?.close()
            fileOutputString?.close()
        }
    }

    suspend fun syncTime(): Boolean = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["year"] = calendar.get(Calendar.YEAR)
            paramMap["month"] = calendar.get(Calendar.MONTH) + 1
            paramMap["day"] = calendar.get(Calendar.DAY_OF_MONTH)
            paramMap["hour"] = calendar.get(Calendar.HOUR_OF_DAY)
            paramMap["min"] = calendar.get(Calendar.MINUTE)
            paramMap["sec"] = calendar.get(Calendar.SECOND)
            paramMap["usec"] = calendar.get(Calendar.MILLISECOND)
            getTS004Service().syncTime(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun syncTimeZone(): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["timezone"] = TimeZone.getDefault().rawOffset / 1000 / 60 / 60
            getTS004Service().syncTimeZone(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getVersion(): TS004Response<VersionBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getVersion()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getDeviceInfo(): TS004Response<DeviceInfo>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getDeviceInfo()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFileCount(fileType: Int): Int? = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["fileType"] = fileType
            getTS004Service().getFileCount(paramMap.toBody()).data?.fileCount ?: 0
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getNewestFile(fileType: Int): List<FileBean>? = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["pageNum"] = 1
            paramMap["pageCount"] = 1
            paramMap["fileType"] = fileType
            getTS004Service().getFileList(paramMap.toBody()).data?.filelist ?: return@withContext ArrayList()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAllFileList(fileType: Int): List<FileBean> = withContext(Dispatchers.IO) {
        try {
            val fileCount = getFileCount(fileType) ?: return@withContext ArrayList()
            if (fileCount < 1) {
                return@withContext ArrayList()
            }

            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["pageNum"] = 1
            paramMap["pageCount"] = fileCount
            paramMap["fileType"] = fileType
            getTS004Service().getFileList(paramMap.toBody()).data?.filelist ?: ArrayList()
        } catch (_: Exception) {
            ArrayList()
        }
    }

    suspend fun getFileByPage(fileType: Int, pageNum: Int, pageCount: Int): List<FileBean>? = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["pageNum"] = pageNum
            paramMap["pageCount"] = pageCount
            paramMap["fileType"] = fileType
            getTS004Service().getFileList(paramMap.toBody()).data?.filelist ?: ArrayList()
        } catch (_: Exception) {
            null
        }
    }

    data class IdData(val id: Int)
    
    suspend fun deleteFiles(ids: Array<Int>): Boolean = withContext(Dispatchers.IO) {
        try {
            val idArray: Array<IdData> = Array(ids.size) {
                IdData(ids[it])
            }

            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["filelist"] = idArray
            getTS004Service().deleteFile(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateFirmware(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val isStartSuccess = getTS004Service().firmwareUpdateStart().isSuccess()
            if (!isStartSuccess) {
                return@withContext false
            }

            val isSendStartSuccess = sendUpgradeFileStart(file)
            if (!isSendStartSuccess) {
                return@withContext false
            }

            val isSendFileSuccess = sendUpgradeFile(file)
            if (!isSendFileSuccess) {
                return@withContext false
            }

            val isEndSuccess = sendUpgradeFileEnd(file)
            if (!isEndSuccess) {
                return@withContext false
            }

            var status = getTS004Service().getUpgradeStatus().data?.status
            while (status == 0 || status == 1 || status == 2) {
                delay(1000)
                status = getTS004Service().getUpgradeStatus().data?.status
            }

            status == 4
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun sendUpgradeFileStart(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["saveAsFile"] = true
            paramMap["MD5"] = EncryptUtils.encryptMD5File2String(file).lowercase(Locale.ROOT)
            paramMap["length"] = file.length()
            getTS004Service().sendUpgradeFileStart(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun sendUpgradeFile(file: File): Boolean = withContext(Dispatchers.IO) {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)

            var hasReadCount = 0
            var byteArray = ByteArray(1024 * 1024 * 5)

            var readCount = fileInputStream.read(byteArray)
            while (readCount != -1) {
                hasReadCount += readCount
                if (hasReadCount == 1024 * 1024 * 5) {
                    getTS004Service().sendUpgradeFile(byteArray.toRequestBody())
                    hasReadCount = 0
                    byteArray = ByteArray(1024 * 1024 * 5)
                }
                readCount = fileInputStream.read(byteArray, hasReadCount, byteArray.size - hasReadCount)
            }

            if (hasReadCount > 0) {
                val lastArray = ByteArray(hasReadCount)
                System.arraycopy(byteArray, 0, lastArray, 0, hasReadCount)
                getTS004Service().sendUpgradeFile(lastArray.toRequestBody())
            }

            true
        } catch (_: Exception) {
            false
        } finally {
            fileInputStream?.close()
        }
    }

    private suspend fun sendUpgradeFileEnd(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["MD5"] = EncryptUtils.encryptMD5File2String(file).lowercase(Locale.ROOT)
            getTS004Service().sendUpgradeFileEnd(paramMap.toBody()).isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun setPseudoColor(mode: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["enable"] = false
            paramMap["mode"] = mode
            getTS004Service().setPseudoColor(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPseudoColor(): TS004Response<PseudoColorBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getPseudoColor()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setRangeFind(state: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["state"] = state
            getTS004Service().setRangeFind(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRangeFind(): TS004Response<RangeBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getRangeFind()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setPanelParam(brightness: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["brightness"] = brightness
            getTS004Service().setPanelParam(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPanelParam(): TS004Response<BrightnessBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getPanelParam()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setPip(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["enable"] = enable
            getTS004Service().setPip(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPip(): TS004Response<PipBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getPip()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setZoom(factor: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["enable"] = true
            paramMap["factor"] = factor
            getTS004Service().setZoom(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getZoom(): TS004Response<ZoomBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getZoom()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setSnapshot(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTS004Service().setSnapshot().isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setVideo(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["enable"] = enable
            getTS004Service().setVRecord(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRecordStatus(): TS004Response<RecordStatusBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getVRecord()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFreeSpace(): FreeSpaceBean? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().freeSpace().data
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFormatStorage(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTS004Service().formatStorage().isSuccess()
        } catch (_: Exception) {
            false
        }
    }
    
    suspend fun getResetAll(): Boolean = withContext(Dispatchers.IO) {
        try {
            getTS004Service().resetAll().status == 100
        } catch (_: Exception) {
            false
        }
    }
    
    suspend fun setTISR(state: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val paramMap: HashMap<String, Any> = HashMap()
            paramMap["state"] = state
            getTS004Service().setTISR(paramMap.toBody()).isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTISR(): TS004Response<TISRBean>? = withContext(Dispatchers.IO) {
        try {
            getTS004Service().getTISR()
        } catch (_: Exception) {
            null
        }
    }
}