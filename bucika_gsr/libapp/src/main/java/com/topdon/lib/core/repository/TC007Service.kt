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

/**
 *
 * Created by LCG on 2024/4/28.
 */
interface TC007Service {
    /**
     * 获取产品信息
     */
    @GET("/v1/system/product/info/dj")
    suspend fun getProductInfo(): TC007Response<ProductBean>

    /**
     * 获取设备电池信息
     */
    @GET("/v1/system/local/battery")
    suspend fun getBatteryInfo(): TC007Response<BatteryInfo>

    /**
     * 同步时间.
     */
    @PUT("/v1/system/local/time")
    suspend fun syncTime(@Body requestBody: RequestBody): TC007Response<Any?>

    /**
     * 固件升级-上传固件升级包
     */
    @Multipart
    @POST("/v1/system/upgrade/package?reset=true")
    suspend fun sendUpgradeFile(
        @Query("filename") filename: String,
        @Query("fileNumber") fileNumber: Int,
        @Query("totalNumber") totalNumber: Int,
        @Query("md5") md5: String,
        @Part part: MultipartBody.Part
    ): TC007Response<Any?>
    /**
     * 查询固件升级状态.
     */
    @GET("/v1/system/upgrade/status")
    suspend fun getUpgradeStatus(): TC007Response<TC07UpgradeStatus>

    /**
     * 恢复出厂设置
     */
    @PUT("/v1/system/magic/factory")
    suspend fun resetToFactory(): TC007Response<Boolean>

    /**
     * 执行锅盖标定
     */
    @PUT("/v1/camera/videoin/thermal/lid")
    suspend fun correction(): TC007Response<Any?>


    /**
     * 获取测温属性参数
     */
    @GET("/v1/thermal/env/attribute?default=false")
    suspend fun getEnvAttr(): TC007Response<EnvAttr>
    /**
     * 设置测温属性参数
     */
    @PUT("/v1/thermal/env/attribute?default=false")
    suspend fun setEnvAttr(@Body requestBody: RequestBody): TC007Response<Any?>


    /**
     * 设置温度修正参数
     */
    @PUT("/v1/thermal/env/target")
    suspend fun setIRConfig(@Body requestBody: RequestBody): TC007Response<Any?>

    @GET("/v1/thermal/temp/frame")
    suspend fun getTempFrame(): TC007Response<TempFrameParam>
    /**
     * 设置整帧测温（中心点、全图最高温、全图最低温）
     */
    @POST("/v1/thermal/temp/frame")
    suspend fun setTempFrame(@Body requestBody: RequestBody): TC007Response<Any?>
    /**
     * 设置测温点
     */
    @POST("/v1/thermal/temp/point")
    suspend fun setTempPoint(@Body requestBody: RequestBody): TC007Response<Any?>
    /**
     * 设置测温线
     */
    @POST("/v1/thermal/temp/line")
    suspend fun setTempLine(@Body requestBody: RequestBody): TC007Response<Any?>
    /**
     * 设置测温面
     */
    @POST("/v1/thermal/temp/rectangle")
    suspend fun setTempRect(@Body requestBody: RequestBody): TC007Response<Any?>



    /**
     * 拍照
     */
    @PUT("/v1/storage/picture/snap/manual")
    suspend fun getPhoto(): TC007Response<PhotoBean>


    /**
     * 设置图像模式
     * 0：红外；1：可见光；2：画中画；3：双光融合；4：细节增强
     */
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
    //双光配准
    @GET("/v1/camera/videoin/registration")
    suspend fun getRegistration(@Query("chn") mode: Int,@Query("default") default : String): TC007Response<WifiAttributeBean?>

    @PUT("/v1/camera/videoin/thermal/pallete/dj")
    suspend fun setPallete(@Body requestBody: RequestBody): TC007Response<Any?>

    /**
     * 获取测温属性参数
     * chn
     * integer
     * 视频通道
     * 可选
     * 示例值:
     * 1
     * default
     * string
     * 可选
     * true：默认配置；false：当前配置
     * 示例值:
     * false
     */
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