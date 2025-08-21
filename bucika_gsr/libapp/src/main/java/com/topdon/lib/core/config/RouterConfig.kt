package com.topdon.lib.core.config

object RouterConfig {

    private const val GROUP_APP = "app"
    private const val GROUP_IR = "ir"
    private const val GROUP_HIK_IR = "irHik"
    private const val GROUP_USER = "user"
    private const val GROUP_REPORT = "report"
    private const val GROUP_THERMAL04 = "ts004"
    private const val GROUP_THERMAL07 = "tc007"
    private const val GROUP_CALIBRATE = "calibrate"


    //app
    const val MAIN = "/$GROUP_APP/main"
    const val CLAUSE = "/$GROUP_APP/clause"
    const val POLICY = "/$GROUP_APP/policy"
    const val VERSION = "/$GROUP_APP/version"
    const val PDF = "/$GROUP_APP/app/pdf"
    const val IR_MORE_HELP = "/$GROUP_APP/app/more_help"
    const val IR_GALLERY_EDIT = "/$GROUP_APP/gallery/edit"
    const val WEB_VIEW = "/$GROUP_APP/WebViewActivity"

    //海康热成像
    const val IR_HIK_MAIN = "/$GROUP_HIK_IR/irHikMain"
    const val IR_HIK_CORRECT_THREE = "/$GROUP_HIK_IR/correction3"
    const val IR_HIK_MONITOR_CAPTURE1 = "/$GROUP_HIK_IR/monitorCap1"
    const val IR_HIK_IMG_PICK = "/$GROUP_HIK_IR/ImagePick"

    //ir
    const val IR_MAIN = "/$GROUP_IR/irMain"
    const val IR_FRAME = "/$GROUP_IR/frame"
    const val IR_SETTING = "/$GROUP_IR/setting"
    const val IR_THERMAL_MONITOR = "/$GROUP_IR/monitor"
    const val IR_MONITOR_CHART = "/$GROUP_IR/monitor/chart"
    const val IR_THERMAL_LOG_MP_CHART = "/$GROUP_IR/log/mp/chart"
    const val IR_GALLERY_HOME = "/$GROUP_IR/gallery/home"
    const val IR_GALLERY_DETAIL_01 = "/$GROUP_IR/gallery/detail01"
    const val IR_GALLERY_DETAIL_04 = "/$GROUP_IR/gallery/detail04"
    const val IR_VIDEO_GSY = "/$GROUP_IR/video/gsy"
    const val IR_CAMERA_SETTING = "/$GROUP_IR/camera/setting"
    const val IR_CORRECTION = "/$GROUP_IR/correction"
    const val IR_CORRECTION_TWO = "/$GROUP_IR/correction2"
    const val IR_CORRECTION_THREE = "/$GROUP_IR/correction3"
    const val IR_CORRECTION_FOUR = "/$GROUP_IR/correction4"
    const val IR_IMG_PICK = "/$GROUP_IR/ImagePickIRActivity"
    const val IR_IMG_PICK_PLUS = "/$GROUP_IR/ImagePickIRPlushActivity"


    const val IR_GALLERY_3D = "/menu/Image3DActivity"

    //TS004
    const val IR_MONOCULAR = "/$GROUP_THERMAL04/IRMonocularActivity"
    const val IR_DEVICE_ADD = "/$GROUP_THERMAL04/DeviceAddActivity"
    const val IR_CONNECT_TIPS = "/$GROUP_THERMAL04/ConnectTipsActivity"

    //TC007
    const val IR_THERMAL_07 = "/$GROUP_THERMAL07/IRThermal07Activity"
    const val IR_MONITOR_CAPTURE_07 = "/$GROUP_THERMAL07/MonitorCapture1"
    const val IR_CORRECTION_07 = "/$GROUP_THERMAL07/IR07CorrectionThreeActivity"
    const val IR_IMG_PICK_07 = "/$GROUP_THERMAL07/ImagePickTC007Activity"


    //report 生成报告
    const val REPORT_CREATE_FIRST = "/$GROUP_REPORT/create/first"
    const val REPORT_CREATE_SECOND = "/$GROUP_REPORT/create/second"
    const val REPORT_PREVIEW_FIRST = "/$GROUP_REPORT/preview/first"
    const val REPORT_PREVIEW_SECOND = "/$GROUP_REPORT/preview/second"
    const val REPORT_DETAIL = "/$GROUP_REPORT/detail"
    const val REPORT_LIST = "/$GROUP_REPORT/list"
    const val REPORT_PICK_IMG = "/$GROUP_REPORT/pick/img"
    const val REPORT_PREVIEW = "/$GROUP_REPORT/preview"


    //user
    const val QUESTION = "/$GROUP_USER/question"//FAQ
    const val QUESTION_DETAILS = "/$GROUP_USER/question/details"//FAQ
    const val UNIT = "/$GROUP_USER/unit"
    const val TS004_MORE = "/$GROUP_USER/ts004More"
    const val TC_MORE = "/$GROUP_USER/tcMore"
    const val DEVICE_INFORMATION = "/$GROUP_USER/device_information"
    const val TISR = "/$GROUP_USER/tisr"
    const val ELECTRONIC_MANUAL = "/$GROUP_USER/electronic_manual"
    const val STORAGE_SPACE = "/$GROUP_USER/storage_space"
    const val AUTO_SAVE = "/$GROUP_USER/auto_save"

    //双光
    const val MANUAL_START = "/$GROUP_CALIBRATE/manual/first"
    const val IR_FRAME_PLUSH = "/$GROUP_IR/frame/plush"

    //lite
    const val IR_TCLITE = "/lite/tcLite"
    const val IR_THERMAL_MONITOR_LITE = "/lite/monitor"
    const val IR_IMG_PICK_LITE = "/lite/ImagePickIRLiteActivity"
    const val IR_MONITOR_CHART_LITE = "/lite/monitor/chart"
    const val IR_CORRECTION_THREE_LITE = "/lite/correction3"
    const val IR_CORRECTION_FOUR_LITE = "/lite/correction4"

}