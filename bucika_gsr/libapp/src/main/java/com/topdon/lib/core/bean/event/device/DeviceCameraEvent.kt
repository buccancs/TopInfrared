package com.topdon.lib.core.bean.event.device

/**
 * @param action
 * 100:初始化
 * 101:有图像
 */
@Deprecated("只有监听没有发送，一个没有用处的 Event")
data class DeviceCameraEvent(val action: Int)
