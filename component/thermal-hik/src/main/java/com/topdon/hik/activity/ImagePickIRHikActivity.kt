package com.topdon.hik.activity

import android.graphics.Bitmap
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.hik.fragment.IRThermalHikFragment
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BasePickImgActivity
import com.topdon.module.thermal.ir.R

@Route(path = RouterConfig.IR_HIK_IMG_PICK)
class ImagePickIRHikActivity : BasePickImgActivity() {

    private var fragment = IRThermalHikFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, fragment)
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as IRThermalHikFragment
        }

    }

    override suspend fun getPickBitmap(): Bitmap = fragment.getBitmap()
}