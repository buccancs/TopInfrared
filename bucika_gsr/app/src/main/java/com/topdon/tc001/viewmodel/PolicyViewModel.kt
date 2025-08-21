package com.topdon.tc001.viewmodel

import androidx.lifecycle.viewModelScope
import com.topdon.lib.core.http.repository.LmsRepository
import com.topdon.lib.core.ktbase.BaseViewModel
import com.topdon.lib.core.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PolicyViewModel : BaseViewModel() {

    val htmlViewData = SingleLiveEvent<HtmlBean>()

    /**
     * @param type 1: 用户服务协议 2: 隐私政策 3: 第三方组件
     */
    fun getUrl(type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val urlType = when (type) {
                1 -> 21
                2 -> 22
                3 -> 23
                else -> 21
            }
            val result = LmsRepository.getStatementUrl(urlType.toString())
            if (result != null && !result.htmlContent.isNullOrBlank()) {
                htmlViewData.postValue(HtmlBean(body = result.htmlContent, action = 1))
            } else {
                htmlViewData.postValue(HtmlBean())
            }
        }
    }

    data class HtmlBean(val body: String? = null, val action: Int = 0)
}