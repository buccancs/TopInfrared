package com.topdon.lib.core.http.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 *
 * Created by LCG on 2024/3/5.
 */
interface DownloadApiService {
    /**
     * 下载文件.
     */
    @GET
    @Streaming
    suspend fun download(@Url url: String): ResponseBody
}