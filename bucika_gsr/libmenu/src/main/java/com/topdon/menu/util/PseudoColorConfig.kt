package com.topdon.menu.util

/**
 * 伪彩菜单、伪彩条所用的预设伪彩配置.
 */
object PseudoColorConfig {

    /**
     * 根据指定伪彩代号获取对应的颜色数组
     * 1-白热 3-铁红 4-彩虹1 5-彩虹2 6-彩虹3 7-红热 8-热铁 9-彩虹4 10-彩虹5 11-黑热
     * @param code 伪彩代号，由于历史遗留（2D编辑的数据、保存设置开关的伪彩）没法改了
     */
    @JvmStatic
    fun getColors(code: Int): IntArray = when (code) {
        1 -> intArrayOf(0xffffffff.toInt(), 0xff000000.toInt())
        3 -> intArrayOf(0xfffbda00.toInt(), 0xffea0e0e.toInt(), 0xff6907af.toInt())
        4 -> intArrayOf(0xffe7321d.toInt(), 0xfffdee38.toInt(), 0xff58e531.toInt(), 0xff0003c8.toInt(), 0xff01000e.toInt())
        5 -> intArrayOf(0xffe7321d.toInt(), 0xfffdee38.toInt(), 0xff65fa33.toInt(), 0xff5aeefd.toInt(), 0xff0d06d2.toInt(), 0xff701b71.toInt())
        6 -> intArrayOf(0xfffce7e5.toInt(), 0xffec361e.toInt(), 0xfffdf339.toInt(), 0xff67f933.toInt(), 0xff2009f8.toInt(), 0xff3e0d8d.toInt(), 0xff060011.toInt())
        7 -> intArrayOf(0xffe83120.toInt(), 0xffc2c2c2.toInt(), 0xff010101.toInt())
        8 -> intArrayOf(0xffec391f.toInt(), 0xfffffe3b.toInt(), 0xff375e5e.toInt(), 0xff000000.toInt())
        9 -> intArrayOf(0xfffdf3fe.toInt(), 0xfff081f7.toInt(), 0xffe2311c.toInt(), 0xfff8d333.toInt(), 0xff67fa43.toInt(), 0xff00066b.toInt(), 0xff000006.toInt())
        10 -> intArrayOf(0xfffffff7.toInt(), 0xfffeff50.toInt(), 0xffe63023.toInt(), 0xffe331e6.toInt(), 0xff56d1fa.toInt(), 0xff5ffa3c.toInt(), 0xff0006d8.toInt(), 0xff000012.toInt())
        11 -> intArrayOf(0xff000000.toInt(), 0xffffffff.toInt())
        else -> intArrayOf(0xfffbda00.toInt(), 0xffea0e0e.toInt(), 0xff6907af.toInt())
    }

    @JvmStatic
    fun getPositions(code: Int): FloatArray = when (code) {
        1 -> floatArrayOf(0f, 1f)
        3 -> floatArrayOf(0f, 0.5f, 1f)
        4 -> floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f)
        5 -> floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f)
        6 -> floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 0.9f, 1f)
        7 -> floatArrayOf(0f, 0.5f, 1f)
        8 -> floatArrayOf(0f, 0.33f, 0.66f, 1f)
        9 -> floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 0.9f, 1f)
        10 -> floatArrayOf(0f, 0.1f, 0.2f, 0.4f, 0.6f, 0.8f, 0.9f, 1f)
        11 -> floatArrayOf(0f, 1f)
        else -> floatArrayOf(0f, 0.5f, 1f)
    }

    @JvmStatic
    fun getSeekBarColors(): IntArray = intArrayOf(0xffdddddd.toInt(), 0xff333333.toInt())

    @JvmStatic
    fun getSeekBarAlpha(): FloatArray = floatArrayOf(0f, 1f)
}