package com.topdon.lib.core.http.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadApiService {
    
    @GET
    @Streaming
    suspend fun download(@Url url: String): ResponseBody
}