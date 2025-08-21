package com.topdon.lib.core.http.tool

import com.topdon.lib.core.http.api.DownloadApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object DownloadTool {

    private fun getOKHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)//不重试
        .connectTimeout(10, TimeUnit.SECONDS)//10秒与默认值一致
        .readTimeout(10, TimeUnit.SECONDS)   //10秒与默认值一致
        .writeTimeout(10, TimeUnit.SECONDS)  //10秒与默认值一致
        .build()

    private fun getService(): DownloadApiService = Retrofit.Builder()
        .baseUrl("http://192.168.40.1:8080")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(getOKHttpClient())
        .build()
        .create(DownloadApiService::class.java)


    suspend fun download(url: String, file: File, listener: (cur: Long, total: Long) -> Unit): Boolean = withContext(Dispatchers.IO) {
        val responseBody = try {
            getService().download(url)
        } catch (_: Exception) {
            return@withContext false
        }
        var inputStream: InputStream? = null
        var fileOutputString: FileOutputStream? = null
        try {
            inputStream = responseBody.byteStream()
            fileOutputString = FileOutputStream(file)

            val totalCount = responseBody.contentLength()
            val buffer = ByteArray(4096)
            var hasReadCount = 0L
            var lastReadCount = 0L
            var readLength = inputStream.read(buffer)
            while (readLength != -1) {
                hasReadCount += readLength
                fileOutputString.write(buffer, 0, readLength)
                if (hasReadCount - lastReadCount > 100 * 1024) {
                    lastReadCount = hasReadCount
                    launch(Dispatchers.Main) {
                        listener.invoke(hasReadCount, totalCount)
                    }
                }

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
}