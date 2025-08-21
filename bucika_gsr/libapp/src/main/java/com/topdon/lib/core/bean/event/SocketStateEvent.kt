package com.topdon.lib.core.bean.event

/**
 * 目标 WIFI 设备（TS004 或 TC007）连接状态变更事件.
 * Created by LCG on 2024/4/23.
 *
 * @param isConnect true-已连接 false-已断开
 * @param isTS004 true-TS004 false-TC007
 */
data class SocketStateEvent(val isConnect: Boolean, val isTS004: Boolean)