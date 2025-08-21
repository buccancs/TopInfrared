package com.topdon.menu.constant

/**
 * 测温模式-菜单5-设置/观测模式-菜单6-设置 菜单类型.
 *
 * Created by LCG on 2024/11/28.
 */
enum class SettingType {
    /** 伪彩条 */
    PSEUDO_BAR,

    /** 对比度 */
    CONTRAST,

    /** 锐度（细节） */
    DETAIL,

    /** 旋转 */
    ROTATE,

    /** 镜像 */
    MIRROR,

    /** 警示 */
    ALARM,

    /** 字体 */
    FONT,

    /** 指南针（仅观测模式） */
    COMPASS,

    /** 水印（仅2D编辑） */
    WATERMARK,
}