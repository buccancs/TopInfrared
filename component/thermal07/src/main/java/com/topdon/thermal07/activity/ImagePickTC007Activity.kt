package com.topdon.thermal07.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BasePickImgActivity
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.module.thermal.ir.R
import com.topdon.tc004.activity.video.PlayFragment
import org.easydarwin.video.Client

@Route(path = RouterConfig.IR_IMG_PICK_07)
class ImagePickTC007Activity : BasePickImgActivity() {
    companion object {
        private const val RTSP_URL = "rtsp://192.168.40.1/stream0"
    }
    var playFragment : PlayFragment ?= null
    override fun initView() {
        if (savedInstanceState == null) {
            playFragment = PlayFragment.newInstance(RTSP_URL, Client.TRANSTYPE_TCP, 1, null,true)
            supportFragmentManager.beginTransaction().add(R.id.fragment_container_view, playFragment!!).commit()
        }
    }

    override suspend fun getPickBitmap(): Bitmap? {
        var resultBitmap : Bitmap ?= null
        val photoBean = TC007Repository.getPhoto()
        if (200 == photoBean?.Code) {
            val bytes64 =
                Base64.decode(photoBean.Data?.IRFile ?: photoBean.Data?.DCFile, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes64!!, 0, bytes64!!.size)
            resultBitmap =  Bitmap.createScaledBitmap(
                bitmap, playFragment!!.surfaceViewWidth,
                playFragment!!.surfaceViewHeight, true
            )
        }
        return resultBitmap
    }

    override fun initData() {

    }

}