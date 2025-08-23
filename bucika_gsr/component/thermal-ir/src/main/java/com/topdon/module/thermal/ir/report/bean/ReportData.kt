package com.topdon.module.thermal.ir.report.bean

import com.blankj.utilcode.util.GsonUtils

class ReportData {
    
    var code = 0
    var data: DataBean? = null
    var msg: String? = null
    var serverTime: String? = null

    class DataBean {
        
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

