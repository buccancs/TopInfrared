package com.topdon.lib.core.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface TC007Service {
    
    @GET("/v1/system/product/info/dj")
    suspend fun getProductInfo(): TC007Response<ProductBean>

    @GET("/v1/system/local/battery")
    suspend fun getBatteryInfo(): TC007Response<BatteryInfo>

    @PUT("/v1/system/local/time")
    suspend fun syncTime(@Body requestBody: RequestBody): TC007Response<Any?>

    @Multipart
    @POST("/v1/system/upgrade/package?reset=true")
    suspend fun sendUpgradeFile(
        @Query("filename") filename: String,
        @Query("fileNumber") fileNumber: Int,
        @Query("totalNumber") totalNumber: Int,
        @Query("md5") md5: String,
        @Part part: MultipartBody.Part
    ): TC007Response<Any?>
    
    @GET("/v1/system/upgrade/status")
    suspend fun getUpgradeStatus(): TC007Response<TC07UpgradeStatus>

    @PUT("/v1/system/magic/factory")
    suspend fun resetToFactory(): TC007Response<Boolean>

    @PUT("/v1/camera/videoin/thermal/lid")
    suspend fun correction(): TC007Response<Any?>

    @GET("/v1/thermal/env/attribute?default=false")
    suspend fun getEnvAttr(): TC007Response<EnvAttr>
    
    @PUT("/v1/thermal/env/attribute?default=false")
    suspend fun setEnvAttr(@Body requestBody: RequestBody): TC007Response<Any?>

    @PUT("/v1/thermal/env/target")
    suspend fun setIRConfig(@Body requestBody: RequestBody): TC007Response<Any?>

    @GET("/v1/thermal/temp/frame")
    suspend fun getTempFrame(): TC007Response<TempFrameParam>
    
    @POST("/v1/thermal/temp/frame")
    suspend fun setTempFrame(@Body requestBody: RequestBody): TC007Response<Any?>
    
    @POST("/v1/thermal/temp/point")
    suspend fun setTempPoint(@Body requestBody: RequestBody): TC007Response<Any?>
    
    @POST("/v1/thermal/temp/line")
    suspend fun setTempLine(@Body requestBody: RequestBody): TC007Response<Any?>
    
    @POST("/v1/thermal/temp/rectangle")
    suspend fun setTempRect(@Body requestBody: RequestBody): TC007Response<Any?>

    @PUT("/v1/storage/picture/snap/manual")
    suspend fun getPhoto(): TC007Response<PhotoBean>

    @PUT("/v1/camera/videoin/mode")
    suspend fun setMode(@Query("mode") mode: Int): TC007Response<Any?>

    @GET("/v1/camera/videoin/mode")
    suspend fun getMode(@Query("mode") mode: Int): TC007Response<Any?>

    @GET("/v1/camera/videoin/fusion/ratio")
    suspend fun getRatio(@Query("default") default: String): TC007Response<WifiAttributeBean?>

    @PUT("/v1/camera/videoin/fusion/ratio")
    suspend fun setRatio(@Body requestBody: RequestBody): TC007Response<Any?>

    @PUT("/v1/camera/videoin/registration")
    suspend fun setRegistration(@Body requestBody: RequestBody): TC007Response<Any?>
    @GET("/v1/camera/videoin/registration")
    suspend fun getRegistration(@Query("chn") mode: Int,@Query("default") default : String): TC007Response<WifiAttributeBean?>

    @PUT("/v1/camera/videoin/thermal/pallete/dj")
    suspend fun setPallete(@Body requestBody: RequestBody): TC007Response<Any?>

    @GET("/v1/thermal/env/attribute")
    suspend fun getAttribute(@Query("chn") mode: Int,@Query("default") default : String): TC007Response<AttributeBean?>

    @POST("/v1/camera/videoin/param")
    suspend fun setParam(@Body requestBody: RequestBody): TC007Response<Any?>

    @POST("/v1/system/local/font")
    suspend fun setFont(@Body requestBody: RequestBody): TC007Response<Any?>
    @PUT("/v1/camera/videoin/thermal/correction")
    suspend fun setCorrection(): TC007Response<Any?>

    @POST("/v1/thermal/temp/isotherm")
    suspend fun setIsotherm(@Body requestBody: RequestBody): TC007Response<Any?>
}