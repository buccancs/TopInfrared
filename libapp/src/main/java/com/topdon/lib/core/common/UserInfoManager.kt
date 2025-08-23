package com.topdon.lib.core.common

import android.text.TextUtils

class UserInfoManager {

    companion object {

        @Volatile
        var manager: UserInfoManager? = null

        fun getInstance(): UserInfoManager {
            if (manager == null) {
                synchronized(UserInfoManager::class) {
                    if (manager == null) {
                        manager = UserInfoManager()
                    }
                }
            }
            return manager!!
        }

    }

    fun isLogin(): Boolean {
        val token = SharedManager.getToken()
        return if (TextUtils.equals("-1", token)) {
            false
        } else {
            !TextUtils.isEmpty(token)
        }
    }

    fun login(
        token: String,
        userId: String,
        phone: String?,
        email: String,
        nickname: String,
        headUrl: String?,
    ) {
        SharedManager.setUserId(userId)
        SharedManager.setUsername(if (getMaskPhone(phone)?.isNotEmpty() == true) getMaskPhone(phone) ?: "" else email)
        SharedManager.setNickname(nickname)
        SharedManager.setHeadIcon(headUrl ?: "12345")
        SharedManager.setToken(token)
    }

    fun logout() {
        SharedManager.setToken("")
        SharedManager.setUserId("0")
        SharedManager.setNickname("")
        SharedManager.setHeadIcon("")
    }

    private fun getMaskPhone(phone: String?): String? {
        return phone?.replace("(\\d{3})\\d{4}(\\d{4})".toRegex(), "$1****$2")
    }
}