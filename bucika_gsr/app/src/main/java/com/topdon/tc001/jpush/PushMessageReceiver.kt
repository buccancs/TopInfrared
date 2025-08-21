//package com.topdon.tc001.jpush
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import cn.jpush.android.api.CmdMessage
//import cn.jpush.android.api.CustomMessage
//import cn.jpush.android.api.JPushInterface
//import cn.jpush.android.api.JPushMessage
//import cn.jpush.android.api.NotificationMessage
//import cn.jpush.android.service.JPushMessageReceiver
//import com.topdon.lib.core.BaseApplication
//import com.topdon.lms.sdk.helper.TagAliasOperatorHelper
//
//class PushMessageReceiver : JPushMessageReceiver(){
//    private val TAG = "PushMessageService"
//
//    override fun onMessage(context: Context?, customMessage: CustomMessage) {
//        Log.e(TAG, "[onMessage] $customMessage")
////        Intent intent = new Intent("com.jiguang.demo.message");
////        intent.putExtra("msg", customMessage.message);
////        context.sendBroadcast(intent);
//    }
//
//    override fun onNotifyMessageOpened(context: Context?, message: NotificationMessage) {
//        Log.e(TAG, "[onNotifyMessageOpened] $message")
//        setZeroBadgeNumber()
//        try {
//            //打开自定义的Activity
//        } catch (throwable: Throwable) {
//        }
//    }
//
//    override fun onInAppMessageClick(context: Context?, notificationMessage: NotificationMessage?) {
//        super.onInAppMessageClick(context, notificationMessage)
//        Log.e(TAG, "[onInAppMessageClick] 用户点击了通知栏按钮")
//    }
//
//    override fun onMultiActionClicked(context: Context?, intent: Intent) {
//        Log.e(TAG, "[onMultiActionClicked] 用户点击了通知栏按钮")
//        setZeroBadgeNumber()
//        val nActionExtra = intent.extras!!.getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA)
//
//        //开发者根据不同 Action 携带的 extra 字段来分配不同的动作。
//        if (nActionExtra == null) {
//            Log.d(TAG, "ACTION_NOTIFICATION_CLICK_ACTION nActionExtra is null")
//            return
//        }
//        if (nActionExtra == "my_extra1") {
//            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮一")
//        } else if (nActionExtra == "my_extra2") {
//            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮二")
//        } else if (nActionExtra == "my_extra3") {
//            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮三")
//        } else {
//            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮未定义")
//        }
//    }
//
//    private fun setZeroBadgeNumber() {
//        Log.e(TAG, "[onMultiActionClicked] 清空角标")
//        JPushInterface.setBadgeNumber(BaseApplication.instance, 0)
//    }
//
//    override fun onNotifyMessageArrived(context: Context?, message: NotificationMessage) {
//        Log.e(TAG, "[onNotifyMessageArrived] $message")
//        setZeroBadgeNumber()
//    }
//
//    override fun onNotifyMessageDismiss(context: Context?, message: NotificationMessage) {
//        Log.e(TAG, "[onNotifyMessageDismiss] $message")
//    }
//
//    override fun onRegister(context: Context, registrationId: String) {
//        Log.e(TAG, "[onRegister] $registrationId")
//        val intent = Intent("com.jiguang.demo.message")
//        intent.putExtra("rid", registrationId)
//        context.sendBroadcast(intent)
//    }
//
//    override fun onConnected(context: Context?, isConnected: Boolean) {
//        Log.e(TAG, "[onConnected] $isConnected")
//        setZeroBadgeNumber()
//    }
//
//    override fun onCommandResult(context: Context?, cmdMessage: CmdMessage) {
//        Log.e(TAG, "[onCommandResult] $cmdMessage")
//    }
//
//    override fun onTagOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
//        TagAliasOperatorHelper.getInstance().onTagOperatorResult(context, jPushMessage)
//        super.onTagOperatorResult(context, jPushMessage)
//        Log.e(TAG, "[onTagOperatorResult]")
//    }
//
//    override fun onCheckTagOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
//        TagAliasOperatorHelper.getInstance().onCheckTagOperatorResult(context, jPushMessage)
//        super.onCheckTagOperatorResult(context, jPushMessage)
//        Log.e(TAG, "[onCheckTagOperatorResult]")
//    }
//
//    override fun onAliasOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
//        TagAliasOperatorHelper.getInstance().onAliasOperatorResult(context, jPushMessage)
//        super.onAliasOperatorResult(context, jPushMessage)
//        Log.e(TAG, "[onAliasOperatorResult]")
//    }
//
//    override fun onMobileNumberOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
//        TagAliasOperatorHelper.getInstance().onMobileNumberOperatorResult(context, jPushMessage)
//        super.onMobileNumberOperatorResult(context, jPushMessage)
//        Log.e(TAG, "[onMobileNumberOperatorResult]")
//    }
//
//    override fun onNotificationSettingsCheck(context: Context?, isOn: Boolean, source: Int) {
//        super.onNotificationSettingsCheck(context, isOn, source)
//        Log.e(TAG, "[onNotificationSettingsCheck] isOn:$isOn,source:$source")
//    }
//
//    override fun onInAppMessageArrived(
//        context: Context?,
//        notificationMessage: NotificationMessage?
//    ) {
//        super.onInAppMessageArrived(context, notificationMessage)
//        Log.e(TAG, "[onInAppMessageArrived]")
//    }
//
//    override fun onPullInAppResult(context: Context?, jPushMessage: JPushMessage?) {
//        super.onPullInAppResult(context, jPushMessage)
//        Log.e(TAG, "[onInAppMessageArrived]")
//    }
//
//    override fun onSspNotificationWillShow(
//        context: Context?,
//        notificationMessage: NotificationMessage?,
//        s: String?
//    ): Boolean {
//        return super.onSspNotificationWillShow(context, notificationMessage, s)
//    }
//
//    override fun onCheckInAppMessageState(context: Context?, s: String?): Byte {
//        return super.onCheckInAppMessageState(context, s)
//    }
//
//    override fun onCheckSspNotificationState(context: Context?, s: String?): Byte {
//        return super.onCheckSspNotificationState(context, s)
//    }
//
//    override fun onGeofenceReceived(context: Context?, s: String?) {
//        super.onGeofenceReceived(context, s)
//        Log.e(TAG, "[onGeofenceReceived]")
//    }
//
//    override fun onGeofenceRegion(context: Context?, s: String?, v: Double, v1: Double) {
//        super.onGeofenceRegion(context, s, v, v1)
//        Log.e(TAG, "[onGeofenceRegion]")
//    }
//
//    override fun onInAppMessageDismiss(
//        context: Context?,
//        notificationMessage: NotificationMessage?
//    ) {
//        super.onInAppMessageDismiss(context, notificationMessage)
//        Log.e(TAG, "[onInAppMessageDismiss]")
//    }
//
//    override fun onInAppMessageUnShow(
//        context: Context?,
//        notificationMessage: NotificationMessage?
//    ) {
//        super.onInAppMessageUnShow(context, notificationMessage)
//        Log.e(TAG, "[onInAppMessageUnShow]")
//    }
//
//    override fun onNotifyMessageUnShow(
//        context: Context?,
//        notificationMessage: NotificationMessage?
//    ) {
//        super.onNotifyMessageUnShow(context, notificationMessage)
//        Log.e(TAG, "[onNotifyMessageUnShow]")
//    }
//
//    override fun onPropertyOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
//        super.onPropertyOperatorResult(context, jPushMessage)
//        Log.e(TAG, "[onPropertyOperatorResult]")
//    }
//}