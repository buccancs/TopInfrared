package com.topdon.lib.core.config.router

import android.content.Context
import android.widget.Toast
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.DegradeService
import com.elvishew.xlog.XLog

@Route(path = "/router/degrade")
class DegradeServiceImpl : DegradeService {

    override fun init(context: Context?) {

    }

    override fun onLost(context: Context?, postcard: Postcard?) {
        if (context != null) {
            Toast.makeText(context, "can't find the path:" + postcard?.path, Toast.LENGTH_SHORT).show()
        }
    }

}