package com.topdon.lib.core.repository

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface TS004Service {

    @POST("/api/v1/system/setPseudoColor")
    suspend fun setPseudoColor(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getPseudoColor")
    suspend fun getPseudoColor(): TS004Response<PseudoColorBean>
    
    @POST("/api/v1/system/setRangeFind")
    suspend fun setRangeFind(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getRangeFind")
    suspend fun getRangeFind(): TS004Response<RangeBean>
    
    @POST("/api/v1/system/setPanelParam")
    suspend fun setPanelParam(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getPanelParam")
    suspend fun getPanelParam(): TS004Response<BrightnessBean>
    
    @POST("/api/v1/system/setPip")
    suspend fun setPip(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getPip")
    suspend fun getPip(): TS004Response<PipBean>
    
    @POST("/api/v1/system/setZoom")
    suspend fun setZoom(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getZoom")
    suspend fun getZoom(): TS004Response<ZoomBean>
    
    @POST("/api/v1/system/snapshot")
    suspend fun setSnapshot(): TS004Response<Boolean>
    
    @POST("/api/v1/system/vrecord")
    suspend fun setVRecord(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getRecordStatus")
    suspend fun getVRecord(): TS004Response<RecordStatusBean>
    
    @GET
    @Streaming
    suspend fun download(@Url url: String): ResponseBody

    @POST("/api/v1/system/setDateTime")
    suspend fun syncTime(@Body requestBody: RequestBody): TS004Response<Boolean>

    @POST("/api/v1/system/setTimeZone")
    suspend fun syncTimeZone(@Body requestBody: RequestBody): TS004Response<Boolean>

    @POST("/api/v1/system/getVersion")
    suspend fun getVersion(): TS004Response<VersionBean>

    @POST("/api/v1/system/getDeviceInfo")
    suspend fun getDeviceInfo(): TS004Response<DeviceInfo>

    @POST("/api/v1/system/getFileCount")
    suspend fun getFileCount(@Body requestBody: RequestBody): TS004Response<FileCountBean>

    @POST("/api/v1/system/getFileList")
    suspend fun getFileList(@Body requestBody: RequestBody): TS004Response<FilePageBean>

    @POST("/api/v1/system/deleteFile")
    suspend fun deleteFile(@Body requestBody: RequestBody): TS004Response<Boolean>

    @POST("/api/v1/system/remoteUpgrade")
    suspend fun firmwareUpdateStart(): TS004Response<Boolean>
    
    @POST("/api/v1/system/sendUpgradeFileStart")
    suspend fun sendUpgradeFileStart(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @Headers("Content-type: application/octet-stream")
    @POST("/api/v1/system/sendUpgradeFileData")
    suspend fun sendUpgradeFile(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/sendUpgradeFileEnd")
    suspend fun sendUpgradeFileEnd(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getUpgradeStatus")
    suspend fun getUpgradeStatus(): TS004Response<UpgradeStatus>

    @POST("/api/v1/system/getFreeSpace")
    suspend fun freeSpace(): TS004Response<FreeSpaceBean>
    
    @POST("/api/v1/system/formatStorage")
    suspend fun formatStorage(): TS004Response<Boolean>
    
    @POST("/api/v1/system/resetAll")
    suspend fun resetAll(): TS004Response<Boolean>
    
    @POST("/api/v1/system/setTISR")
    suspend fun setTISR(@Body requestBody: RequestBody): TS004Response<Boolean>
    
    @POST("/api/v1/system/getTISR")
    suspend fun getTISR(): TS004Response<TISRBean>
}