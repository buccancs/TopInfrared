package com.topdon.lib.core.config

import com.topdon.lib.core.repository.GalleryRepository

object ExtraKeyConfig {
    /**
     * boolean 类型 - 跳转相册界面时，是否为生成报告拾取图片操作.
     * true-生成报告拾取图片 false-普通的相册浏览
     */
    const val IS_PICK_REPORT_IMG = "IS_PICK_REPORT_IMG"
    /**
     * boolean 类型 - 是否为视频.
     * true-视频 false-图片
     */
    const val IS_VIDEO = "IS_VIDEO"
    /**
     * boolean 类型 - 图库是否有返回箭头
     */
    const val HAS_BACK_ICON = "HAS_BACK_ICON"
    /**
     * boolean 类型 - 图库是否可切换 有线设备、TS004、TC007 目录
     */
    const val CAN_SWITCH_DIR = "CAN_SWITCH_DIR"
    /**
     * boolean 类型 - 设备类型是否为 TC007.
     * true-TC007 false-其他
     */
    const val IS_TC007 = "IS_TC007"
    /**
     * boolean 类型 - 是否拾取检测师签名.
     * true-检测师签名 false-房主签名
     */
    const val IS_PICK_INSPECTOR = "IS_PICK_INSPECTOR"
    /**
     * boolean 类型 - 是否查看报告
     * true-查看报告 false-生成报告
     */
    const val IS_REPORT = "IS_REPORT"


    /**
     * Int 类型 - 进入图库时初始的目录类型 具体取值由 [GalleryRepository.DirType] 定义
     */
    const val DIR_TYPE = "CUR_DIR_TYPE"
    /**
     * Int 类型 - 当前要查看的图片在图片列表中的 index.
     */
    const val CURRENT_ITEM = "CURRENT_ITEM"


    /**
     * Long 类型 - 房屋检测模块：要编辑的检测 Id.
     */
    const val DETECT_ID = "DETECT_ID"
    /**
     * Long 类型 - 房屋检测模块：要编辑的目录 Id.
     */
    const val DIR_ID = "DIR_ID"
    /**
     * Long 类型 - ID.
     */
    const val LONG_ID = "LONG_ID"


    /**
     * String 类型 - URL.
     */
    const val URL = "URL"
    /**
     * String 类型 - 文件绝对路径.
     */
    const val FILE_ABSOLUTE_PATH = "FILE_ABSOLUTE_PATH"
    /**
     * String 类型 - 房屋检测一项 item 名称.
     */
    const val ITEM_NAME = "ITEM_NAME"

    /**
     * String 类型 - 返回输入的文字内容.
     */
    const val RESULT_INPUT_TEXT = "RESULT_INPUT_TEXT"

    /**
     * String 类型 - 拾取的图片在本地的绝对路径.
     */
    const val RESULT_IMAGE_PATH = "RESULT_IMAGE_PATH"
    /**
     * String 类型 - 拾取的白色画笔版签名图片在本地的绝对路径.
     */
    const val RESULT_PATH_WHITE = "RESULT_PATH_WHITE"
    /**
     * String 类型 - 拾取的黑色画笔版签名图片在本地的绝对路径.
     */
    const val RESULT_PATH_BLACK = "RESULT_PATH_BLACK"


    /**
     * List&lt;String&gt; 类型 - 图片在本地绝对路径列表.
     */
    const val IMAGE_PATH_LIST = "IMAGE_PATH_LIST"


    /**
     * Parcelable 类型 - 一张图片点线面信息封装 (ImageTempBean).
     */
    const val IMAGE_TEMP_BEAN = "IMAGE_TEMP_BEAN"
    /**
     * Parcelable 类型 - 一份报告所有信息 (ReportBean).
     */
    const val REPORT_BEAN = "REPORT_BEAN"
    /**
     * Parcelable 类型 - 报告信息 (ReportInfoBean).
     */
    const val REPORT_INFO = "REPORT_INFO"
    /**
     * Parcelable 类型 - 检测条件 (ReportConditionBean).
     */
    const val REPORT_CONDITION = "REPORT_CONDITION"

    /**
     * Parcelable 类型 - 当前已添加的图片对应数据列表 (List<ReportIRBean>).
     */
    const val REPORT_IR_LIST = "REPORT_IR_LIST"


    /**
     * Parcelable 类型 - 自定义渲染设置相关配置项 (CustomPseudoBean).
     */
    const val CUSTOM_PSEUDO_BEAN = "CUSTOM_PSEUDO_BEAN"

    /**
     * long 类型 - Unix 时间戳，单位毫秒.
     */
    const val TIME_MILLIS = "TIME_MILLIS"


    /**
     * String 类型 - 监控记录类型.
     * 由于历史原因，此处使用 String表示：
     * point-点 line-线 fence-面
     */
    const val MONITOR_TYPE = "MONITOR_TYPE"


    const val IR_PATH = "ir_path"
    const val TEMP_HIGH = "temp_high"
    const val TEMP_LOW = "temp_low"

    const val IS_CAR_DETECT_ENTER = "IS_CAR_DETECT_ENTER"


}