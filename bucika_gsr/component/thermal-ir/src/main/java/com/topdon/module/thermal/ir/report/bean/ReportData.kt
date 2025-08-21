package com.topdon.module.thermal.ir.report.bean

import com.blankj.utilcode.util.GsonUtils

/**
 * @author: CaiSongL
 * @date: 2023/5/29 18:00
 */
/**
 * @author: CaiSongL
 * @date: 2023/5/12 17:44
 */
class ReportData {
    /**
     * code : 2000
     * data : {"total":0,"current":1,"hitCount":false,"pages":0,"size":10,"optimizeCountSql":true,"records":[],"searchCount":true,"orders":[]}
     * msg : 操作成功
     * serverTime : 2023-05-13 06:54:10
     */
    var code = 0
    var data: DataBean? = null
    var msg: String? = null
    var serverTime: String? = null

    class DataBean {
        /**
         * total : 0
         * current : 1
         * hitCount : false
         * pages : 0
         * size : 10
         * optimizeCountSql : true
         * records : []
         * searchCount : true
         * orders : []
         */
        var total = 0
        var current = 0
        var isHitCount = false
        var pages = 0
        var size = 0
        var isOptimizeCountSql = false
        var isSearchCount = false
        var records: MutableList<Records?>? = null
    }

    class Records {
        var testReportId: String? = null
        var testTime: String? = null
        var testInfo: String? = null
        var sn: String? = null
        var uploadTime: String? = null
        var status: String? = null
        var isShowTitleTime : Boolean = false
        var reportContent : ReportBean? = null
            get() {
                if (field == null) {
                    field = GsonUtils.fromJson(testInfo, ReportBean::class.java)
                }
                return field
            }

    }
}

