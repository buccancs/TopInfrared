package com.topdon.libcom.util

import android.app.Activity
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.common.WifiSaveSettingUtil
import com.topdon.lib.core.config.ExtraKeyConfig.RESULT_IMAGE_PATH
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.lib.core.tools.ToastTools

object ARouterUtil {
    
    fun jumpImagePick(activity: Activity,isTC007 : Boolean,imgPath : String){
        if (isTC007){
            ARouter.getInstance().build(RouterConfig.IR_IMG_PICK_07).withString(RESULT_IMAGE_PATH,imgPath).navigation(activity,101)
            return
        }
        if (DeviceTools.isTC001PlusConnect()){
            ARouter.getInstance().build(RouterConfig.IR_IMG_PICK_PLUS).withString(RESULT_IMAGE_PATH,imgPath).navigation(activity,101)
        }else if (DeviceTools.isTC001LiteConnect()){
            ARouter.getInstance().build(RouterConfig.IR_IMG_PICK_LITE).withString(RESULT_IMAGE_PATH,imgPath).navigation(activity,101)
        } else if (DeviceTools.isHikConnect()) {
            ARouter.getInstance().build(RouterConfig.IR_HIK_IMG_PICK).withString(RESULT_IMAGE_PATH,imgPath).navigation(activity,101)
        } else{
            ARouter.getInstance().build(RouterConfig.IR_IMG_PICK).withString(RESULT_IMAGE_PATH,imgPath).navigation(activity,101)
        }
    }

}