package com.topdon.lib.core.bean.response

data class ResponseUserInfo(
    val topdonId: String,
    val userName: String,
    val email: String,
    val url: String,
    val pwd: String,
    val remark: String,
    val createTime: Long,
    val updateTime: Long,
    val profilePicture: String,
    val lastVisitTime: String,
    val phone:String?,
    val avatar:String?,
)